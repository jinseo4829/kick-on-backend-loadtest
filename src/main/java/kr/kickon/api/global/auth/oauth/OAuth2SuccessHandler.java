package kr.kickon.api.global.auth.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.auth.jwt.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("🎉 OAuth2 로그인 성공!");
        log.info("   - 요청 URI: {}", request.getRequestURI());
        log.info("   - 요청 URL: {}", request.getRequestURL());
        log.info("   - 요청 도메인: {}", request.getServerName());
        
        TokenDto tokenDto = jwtTokenProvider.createToken(authentication);
        log.info("✅ 토큰 생성 완료");

        // 클라이언트에서 전달한 redirect_uri 파라미터를 얻기
        String redirectUri = request.getParameter("state");
        log.info("🔗 리다이렉트 URI: {}", redirectUri);
        
        // redirect_uri가 없으면 기본값을 설정하거나 예외 처리
        if (redirectUri == null || redirectUri.isEmpty()) {
            redirectUri = "http://localhost:3000";  // 예시로 기본 프론트엔드 URL 설정
            log.info("⚠️ state 파라미터가 없어서 기본값 사용: {}", redirectUri);
        }
        
        // 리다이렉트 URI에서 도메인 추출하여 쿠키 도메인 결정
        String cookieDomain = determineCookieDomain(redirectUri);
        log.info("🍪 결정된 쿠키 도메인: {}", cookieDomain);
        
        // 쿠키에 토큰 설정 (리다이렉트 도메인 기반)
        jwtTokenProvider.setTokenCookies(response, tokenDto, cookieDomain);
        
        log.info("🚀 최종 리다이렉트: {}", redirectUri);
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
    
    /**
     * 리다이렉트 URI를 기반으로 적절한 쿠키 도메인을 결정합니다.
     */
    private String determineCookieDomain(String redirectUri) {
        try {
            java.net.URI uri = new java.net.URI(redirectUri);
            String host = uri.getHost();
            
            log.info("🔍 URI 분석 - Host: {}", host);
            
            if (host == null) {
                return null;
            }
            
            if (host.contains("localhost")) {
                // localhost 환경에서는 localhost 도메인으로 설정
                return "localhost";
            } else if (host.contains("dev.kick-on.kr")) {
                // dev.kick-on.kr 도메인이면 .kick-on.kr 사용 (하위 도메인 공유)
                return ".kick-on.kr";
            } else if (host.contains("kick-on.kr")) {
                // 기타 kick-on.kr 도메인이면 .kick-on.kr 사용
                return ".kick-on.kr";
            } else {
                // 기타 도메인은 null로 설정
                return null;
            }
        } catch (Exception e) {
            log.error("❌ URI 분석 실패: {}", e.getMessage());
            return null;
        }
    }

    private void setTokenCookies(HttpServletResponse response, TokenDto tokenDto) {
        // Access Token 쿠키 설정
        Cookie accessTokenCookie = new Cookie("accessToken", tokenDto.getAccessToken());
        accessTokenCookie.setHttpOnly(true);  // XSS 공격 방지
        accessTokenCookie.setSecure(false);   // 개발환경에서는 false, 배포시 수정 true
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60 * 24); // 24시간 (초 단위)
        // accessTokenCookie.setAttribute("SameSite", "Lax"); // CSRF 방지
        response.addCookie(accessTokenCookie);

        // Refresh Token 쿠키 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", tokenDto.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);   // 개발환경에서는 false, 배포시 수정 true
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 14일 (초 단위)
        // refreshTokenCookie.setAttribute("SameSite", "Lax");
        response.addCookie(refreshTokenCookie);

        log.info("✅ 토큰이 쿠키에 설정되었습니다.");
    }
}