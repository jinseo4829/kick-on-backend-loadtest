package kr.kickon.api.global.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.auth.jwt.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("🎉 OAuth2 로그인 성공!");
        log.info("   - 백엔드 도메인: {}", request.getServerName());
        log.info("   - Active Profile: {}", activeProfile);

        TokenDto tokenDto = jwtTokenProvider.createToken(authentication);
        log.info("✅ 토큰 생성 완료");

        String redirectUri = request.getParameter("state");
        if (redirectUri == null || redirectUri.isEmpty()) {
            redirectUri = "http://localhost:3000";
            log.warn("⚠️ state 파라미터 없음, 기본값 사용: {}", redirectUri);
        }
        log.info("🔗 리다이렉트 URI: {}", redirectUri);

        // yml 설정 그대로 사용
        jwtTokenProvider.setTokenCookies(response, tokenDto);
        log.info("쿠키 설정 완료");

        // 2. Dev 환경 + localhost 조합이면 URL에도 토큰 추가
        if ("dev".equals(activeProfile) && isLocalhost(redirectUri)) {
            log.warn("쿠키 + URL 토큰 둘 다 전송");
            redirectUri = addTokensToUrl(redirectUri, tokenDto);
        } else {
            log.info("쿠키만 사용");
        }

        log.info("🚀 최종 리다이렉트: {}", redirectUri);
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }

    /**
     * redirectUri가 localhost인지 확인
     */
    private boolean isLocalhost(String redirectUri) {
        try {
            java.net.URI uri = new java.net.URI(redirectUri);
            String host = uri.getHost();
            boolean result = host != null && (host.contains("localhost") || host.equals("127.0.0.1"));
            log.info("localhost 체크: {} → {}", host, result);
            return result;
        } catch (Exception e) {
            log.error("URI 분석 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * URL에 토큰 파라미터 추가
     */
    private String addTokensToUrl(String redirectUri, TokenDto tokenDto) {
        String separator = redirectUri.contains("?") ? "&" : "?";
        return redirectUri + separator
                + "accessToken=" + tokenDto.getAccessToken()
                + "&refreshToken=" + tokenDto.getRefreshToken();
    }
}