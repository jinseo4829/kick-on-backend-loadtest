package kr.kickon.api.global.auth.jwt.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.global.auth.jwt.CustomAuthenticationEntryPoint;
import kr.kickon.api.global.auth.jwt.dto.PrincipalUserDetail;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.error.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
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
    private final UserService userService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public int tokenPrefixLength = JwtTokenProvider.TOKEN_PREFIX.length();
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
        try {
//            log.info("🟢 JwtAuthenticationFilter activated for URI: {}", request.getRequestURI());
            Authentication authentication;
            String requestUri = request.getRequestURI();
            String jwt;
            jwt = resolveToken(request);

            // /api/ 경로만 처리
            if (!requestUri.startsWith("/api")) {
                filterChain.doFilter(request, response); // 넘어가
                return;
            }

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 토큰이 유효하면, 사용자 정보 가져오기
                authentication = jwtTokenProvider.getAuthentication(jwt);
                log.error(authentication.getAuthorities().toString());
                System.out.println(authentication.getName());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else{
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

        }catch (Exception ex){
            authenticationEntryPoint.commence(request, response,
                    new InsufficientAuthenticationException(ex.getMessage(), ex));
            return;
        }
        filterChain.doFilter(request, response);
    }
}