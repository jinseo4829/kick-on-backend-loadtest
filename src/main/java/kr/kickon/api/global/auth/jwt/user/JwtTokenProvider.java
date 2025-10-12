package kr.kickon.api.global.auth.jwt.user;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.global.auth.jwt.dto.TokenDto;
import kr.kickon.api.global.auth.jwt.dto.PrincipalUserDetail;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.config.CookieConfig;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.error.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class  JwtTokenProvider{
    public static final String TOKEN_PREFIX = "Bearer ";
    public final String AUTH_PK = "pk";
    public final String AUTHORITIES_KEY = "auth";

    private final String key;
    private final long accessTokenValidityMilliSeconds;
    private final long refreshTokenValidityMilliSeconds;
    private final UserService userService;
    private final CookieConfig cookieConfig;

    public JwtTokenProvider(
            @Value("${jwt.secret_key}")
            String key,
            @Value("${jwt.access-token-validity-in-seconds}")
            long accessTokenValidityMilliSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}")
            long refreshTokenValidityMilliSeconds,
            UserService userService,
            CookieConfig cookieConfig) {
        this.key = key;
        this.accessTokenValidityMilliSeconds = accessTokenValidityMilliSeconds;
        this.refreshTokenValidityMilliSeconds = refreshTokenValidityMilliSeconds;
        this.userService = userService;
        this.cookieConfig = cookieConfig;
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createRefreshToken(Map<String, Object> claims,Long pk) {
        long now = (new Date()).getTime();
        Date refreshValidity = new Date(now + this.refreshTokenValidityMilliSeconds* 1000);
        return Jwts.builder()
                .claims(claims)
                .expiration(refreshValidity)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String createAccessToken(Map<String, Object> claims,Long pk, String authorities) {
        long now = (new Date()).getTime();
        Date accessValidity = new Date(now + this.accessTokenValidityMilliSeconds* 1000);
        return Jwts.builder()
                .claims(claims)
                .subject(pk + "_" + authorities)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(accessValidity)
                .signWith(getSignInKey(),Jwts.SIG.HS256)
                .compact();
    }

    public String createPermanentAccessToken(Map<String, Object> claims, Long pk, String authorities) {
        Date farFutureDate = new Date(System.currentTimeMillis() + (100L * 365 * 24 * 60 * 60 * 1000)); // 100년
        return Jwts.builder()
                .claims(claims)
                .subject(pk + "_" + authorities)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(farFutureDate)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    // access, refresh Token 생성
    public TokenDto createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        User user = userService.findByPk(Long.parseLong(oAuth2User.getName()));
        if(user==null) throw new NotFoundException(ResponseCode.NOT_FOUND_USER);

        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTH_PK, user.getPk());
        claims.put(AUTHORITIES_KEY, authorities);
        String refreshToken = createRefreshToken(claims, user.getPk());
        String accessToken = createAccessToken(claims, user.getPk(), authorities);

        return TokenDto.of(accessToken, refreshToken);
    }

    // token이 유효한 지 검사
    public boolean validateToken(String token) throws AuthenticationException {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            throw new UnauthorizedException(ResponseCode.INVALID_TOKEN,e.getMessage());
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUserAuthorityFromClaims(Claims claims) {
        return claims.get(AUTHORITIES_KEY).toString();
    }

    // token으로부터 Authentication 객체를 만들어 리턴하는 메소드
    public Authentication getAuthentication(String token) throws UnauthorizedException {
        try {
            Claims claims =  getClaimsFromToken(token);
            if (claims == null) {
                throw new UnauthorizedException(ResponseCode.INVALID_TOKEN);
            }
            User user = userService.findByPk(Long.parseLong(claims.get(AUTH_PK).toString()));
            if(user == null) throw new UnauthorizedException(ResponseCode.INVALID_TOKEN);

            String authority = getUserAuthorityFromClaims(claims);
            PrincipalUserDetail principal = new PrincipalUserDetail(
                    user,
                    authority);
            List<SimpleGrantedAuthority> authorities = Arrays.stream(authority.split(","))
                    .map(String::trim)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(principal, null, authorities);
        } catch (Exception e) {
            throw new UnauthorizedException(ResponseCode.INVALID_TOKEN);
        }
    }

    public User getUserFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PrincipalUserDetail principalUserDetail = null;
        User user = null;
        if (authentication != null) {
            principalUserDetail = (PrincipalUserDetail) authentication.getPrincipal();
        }
        if(principalUserDetail==null) throw new UnauthorizedException(ResponseCode.INVALID_TOKEN);

        if(principalUserDetail.getName().equals(String.valueOf(-1))){
            // principalUserDetail에 UserPk가 -1이면 익명 사용자임
        }else {
            user = userService.findByPk(Long.parseLong(principalUserDetail.getName()));
            if(user==null) throw new UnauthorizedException(ResponseCode.INVALID_TOKEN);
        }

        return user;
    }

    public void setTokenCookies(HttpServletResponse response, TokenDto tokenDto) {
        boolean isSecure = cookieConfig.isSecure();
        String domain = cookieConfig.getDomain();
        String sameSite = cookieConfig.getSameSite();

        log.info("🍪 쿠키 설정 - domain: {}, secure: {}", domain, isSecure); // ⭐ 로그 추가

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenDto.getAccessToken())
                .httpOnly(false)
                .secure(isSecure)
                .path("/")
                .domain(domain)
                .maxAge(accessTokenValidityMilliSeconds)
                .sameSite(sameSite)
                .build();
        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenDto.getRefreshToken())
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .domain(domain)
                .maxAge(refreshTokenValidityMilliSeconds)
                .sameSite(sameSite)
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }
}