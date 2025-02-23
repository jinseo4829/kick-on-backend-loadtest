package kr.kickon.api.global.auth.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kickon.api.global.auth.oauth.PrincipalUserDetail;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.error.JwtAuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public final String AUTHORIZATION_HEADER = "Authorization";
    private final JwtTokenProvider jwtTokenProvider;
    public int tokenPrefixLength = JwtTokenProvider.TOKEN_PREFIX.length();
    private final List<String> protectedUris = Arrays.asList("/really","/need-jwt"); // JWT가 필요한 URI 목록

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtTokenProvider.TOKEN_PREFIX)) {
            if (bearerToken.length() > tokenPrefixLength) {
                return bearerToken.substring(tokenPrefixLength);
            }
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication;
        String requestUri = request.getRequestURI();
        String jwt;
        jwt = resolveToken(request);
        if (isProtectedUri(requestUri) && (!StringUtils.hasText(jwt) || !jwtTokenProvider.validateToken(jwt))) throw new JwtAuthenticationException("이거 jwt 있어야 되어유~!",new Throwable("INVALID_JWT"));

        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            // 토큰이 유효하면, 사용자 정보 가져오기
            Claims claims = jwtTokenProvider.getClaimsFromToken(jwt);
            User user = jwtTokenProvider.getUserByClaims(claims);
            Long userPk = user.getPk();
            String authority = jwtTokenProvider.getUserAuthorityFromClaims(claims);

            // Spring Security에 추가
            try {
                authentication = jwtTokenProvider.getAuthentication(jwt);
                if (authentication == null) {
                    throw new IllegalStateException("Authentication object is null!");
                }
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
            }

            // 헤더에 user 정보 추가
            response.setHeader("User-pk", userPk.toString());
            response.setHeader("User-authority", authority);
        }else{
            response.setHeader("User-authority", "ROLE_GUEST");
            String authority = "ROLE_GUEST";
            List<SimpleGrantedAuthority> authorities = Arrays.stream(authority.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            User guestUser = new User();  // 비회원 사용자 객체 생성
            guestUser.setPk((long) -1);
            PrincipalUserDetail principal = new PrincipalUserDetail(
                    guestUser,
                    authority);
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal,null, authorities));

        }
        filterChain.doFilter(request, response);
    }
    // 특정 URI가 JWT 인증을 요구하는지 확인
    private boolean isProtectedUri(String uri) {
        for (String protectedUri : protectedUris) {
            if (uri.matches(protectedUri)) {
                return true;
            }
        }
        return false;
    }
}