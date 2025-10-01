package kr.kickon.api.global.auth.jwt.admin;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.kickon.api.admin.root.AdminService;
import kr.kickon.api.global.auth.jwt.dto.PrincipalAdminDetail;
import kr.kickon.api.global.auth.jwt.dto.TokenDto;
import kr.kickon.api.global.common.entities.Admin;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.config.CookieConfig;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.error.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class AdminJwtTokenProvider {
    public static final String TOKEN_PREFIX = "Bearer ";
    public final String AUTH_PK = "pk";
    public final String AUTHORITIES_KEY = "auth";

    private final String key;
    private final long accessTokenValidityMilliSeconds;
    private final long refreshTokenValidityMilliSeconds;
    private final AdminService adminService;
    private final CookieConfig cookieConfig;

    public AdminJwtTokenProvider(
            @Value("${jwt.admin_secret_key}") String key,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityMilliSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityMilliSeconds,
            AdminService adminService,
            CookieConfig cookieConfig) {
        this.key = key;
        this.accessTokenValidityMilliSeconds = accessTokenValidityMilliSeconds;
        this.refreshTokenValidityMilliSeconds = refreshTokenValidityMilliSeconds;
        this.adminService = adminService;
        this.cookieConfig = cookieConfig;
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createRefreshToken(Map<String, Object> claims, Long pk) {
        long now = (new Date()).getTime();
        Date refreshValidity = new Date(now + this.refreshTokenValidityMilliSeconds * 1000);
        return Jwts.builder()
                .claims(claims)
                .expiration(refreshValidity)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String createAccessToken(Map<String, Object> claims, Long pk, String authorities) {
        long now = (new Date()).getTime();
        Date accessValidity = new Date(now + this.accessTokenValidityMilliSeconds * 1000);
        return Jwts.builder()
                .claims(claims)
                .subject(pk + "_" + authorities)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(accessValidity)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public TokenDto createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        PrincipalAdminDetail oAuth2User = (PrincipalAdminDetail) authentication.getPrincipal();

        Admin admin = adminService.findByPk(Long.parseLong(oAuth2User.getPk()));
        if (admin == null) throw new NotFoundException(ResponseCode.NOT_FOUND_ADMIN);

        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTH_PK, admin.getPk());
        claims.put(AUTHORITIES_KEY, authorities);
        String refreshToken = createRefreshToken(claims, admin.getPk());
        String accessToken = createAccessToken(claims, admin.getPk(), authorities);

        return TokenDto.of(accessToken, refreshToken);
    }

    // Admin용 쿠키 설정
    public void setTokenCookies(HttpServletResponse response, TokenDto tokenDto) {
        boolean isSecure = cookieConfig.isSecure();

        // Admin Access Token 쿠키 설정 (일반 쿠키)
        Cookie accessTokenCookie = new Cookie("adminAccessToken", tokenDto.getAccessToken());
        accessTokenCookie.setHttpOnly(false);
        accessTokenCookie.setSecure(isSecure);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) accessTokenValidityMilliSeconds); // 초 단위
        response.addCookie(accessTokenCookie);

        // Admin Refresh Token 쿠키 설정
        Cookie refreshTokenCookie = new Cookie("adminRefreshToken", tokenDto.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(isSecure); // dev: false, prod: true
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) refreshTokenValidityMilliSeconds); // 초 단위
        response.addCookie(refreshTokenCookie);

        log.info("✅ Admin 토큰이 쿠키에 설정되었습니다.");
    }

    public boolean validateToken(String token) throws AuthenticationException {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
//            System.out.println(e.getMessage());
            throw new UnauthorizedException(ResponseCode.INVALID_TOKEN, e.getMessage());
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

    public Authentication getAuthentication(String token) throws UnauthorizedException {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) throw new UnauthorizedException(ResponseCode.INVALID_TOKEN);

            Admin admin = adminService.findByPk(Long.parseLong(claims.get(AUTH_PK).toString()));
            if (admin == null) throw new UnauthorizedException(ResponseCode.INVALID_TOKEN);

            String authority = getUserAuthorityFromClaims(claims);
            PrincipalAdminDetail principal = new PrincipalAdminDetail(admin, authority);
            List<SimpleGrantedAuthority> authorities = Arrays.stream(authority.split(","))
                    .map(String::trim)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(principal, null, authorities);
        } catch (Exception e) {
            throw new UnauthorizedException(ResponseCode.INVALID_TOKEN);
        }
    }

    public Admin getAdminFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalAdminDetail)) {
            throw new UnauthorizedException(ResponseCode.INVALID_TOKEN);
        }

        PrincipalAdminDetail principalAdminDetail = (PrincipalAdminDetail) authentication.getPrincipal();
        Admin admin = adminService.findByPk(Long.parseLong(principalAdminDetail.getPk()));
        if (admin == null) throw new UnauthorizedException(ResponseCode.INVALID_TOKEN);

        return admin;
    }
}