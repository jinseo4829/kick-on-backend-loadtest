package kr.kickon.api.global.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.global.auth.oauth.PrincipalUserDetail;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.error.JwtAuthenticationException;
import kr.kickon.api.global.error.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider{
    public static final String TOKEN_PREFIX = "Bearer ";
    private static final String AUTH_PK = "pk";
    private static final String AUTHORITIES_KEY = "auth";

    private final String key;
    private final long accessTokenValidityMilliSeconds;
    private final long refreshTokenValidityMilliSeconds;
    private final UserService userService;
    public JwtTokenProvider(
            @Value("${jwt.secret_key}")
            String key,
            @Value("${jwt.access-token-validity-in-seconds}")
            long accessTokenValidityMilliSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}")
            long refreshTokenValidityMilliSeconds,
            UserService userService) {
        this.key = key;
        this.accessTokenValidityMilliSeconds = accessTokenValidityMilliSeconds;
        this.refreshTokenValidityMilliSeconds = refreshTokenValidityMilliSeconds;
        this.userService = userService;
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // access, refresh Token 생성
    public TokenDto createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Optional<User> userOptional = userService.findByPk(Long.parseLong(oAuth2User.getName()));

        if(userOptional.isEmpty()) {
            throw new UserNotFoundException(oAuth2User.getName());
        }
        User user = userOptional.get();

        long now = (new Date()).getTime();

        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTH_PK, user.getPk());
        claims.put(AUTHORITIES_KEY, authorities);
        Date accessValidity = new Date(now + this.accessTokenValidityMilliSeconds* 1000);
        Date refreshValidity = new Date(now + this.refreshTokenValidityMilliSeconds* 1000);
        String accessToken = Jwts.builder()
                .claims(claims)
                .subject(user.getPk() + "_" + authorities)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(accessValidity)
                .signWith(getSignInKey(),Jwts.SIG.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .claims(claims)
                .expiration(refreshValidity)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();

        return TokenDto.of(accessToken, refreshToken);
    }

    // token이 유효한 지 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error(e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error(e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    // token이 만료되었는지 검사
    public boolean validateExpire(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        }
    }


    public User getUserByClaims(Claims claims){
        Optional<User> user = userService.findByPk(Long.parseLong(claims.get(AUTH_PK).toString()));
        if(user.isEmpty()) throw new AuthenticationServiceException("해당 jwt의 유저가 존재하지 않습니다.");
        return user.get();
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
    public Authentication getAuthentication(String token) throws Exception{
        try {
            Claims claims =  getClaimsFromToken(token);
            if (claims == null) {
                log.error("Invalid JWT claims.");
                return null;
            }
            User user = getUserByClaims(claims);
            String authority = getUserAuthorityFromClaims(claims);
            PrincipalUserDetail principal = new PrincipalUserDetail(
                    user,
                    authority);
            List<SimpleGrantedAuthority> authorities = Arrays.stream(authority.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(principal, null, authorities);
        } catch (Exception e) {
            log.error("Error during JWT authentication", e);
            throw new JwtAuthenticationException("Failed to authenticate JWT", e);
        }


    }
}