package kr.kickon.api.global.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.global.auth.oauth.dto.PrincipalUserDetail;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.ForbiddenException;
import kr.kickon.api.global.error.exceptions.JwtAuthenticationException;
import kr.kickon.api.global.error.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
    private final UserService userService;
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
        try {
            Authentication authentication;
            String requestUri = request.getRequestURI();
            String jwt;
            jwt = resolveToken(request);
            if (isProtectedUri(requestUri) && (!StringUtils.hasText(jwt) || !jwtTokenProvider.validateToken(jwt))) throw new ForbiddenException(ResponseCode.FORBIDDEN);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 토큰이 유효하면, 사용자 정보 가져오기
                authentication = jwtTokenProvider.getAuthentication(jwt);
                log.error(authentication.getAuthorities().toString());
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

        }catch (Exception e) {
//            throw new UnauthorizedException(ResponseCode.UNAUTHORIZED).toAuthenticationException();
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