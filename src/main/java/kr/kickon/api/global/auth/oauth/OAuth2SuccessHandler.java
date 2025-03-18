package kr.kickon.api.global.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
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
        TokenDto tokenDto = jwtTokenProvider.createToken(authentication);

        // 클라이언트에서 전달한 redirect_uri 파라미터를 얻기
        String redirectUri = request.getParameter("state");
        log.info("OAuth2 Success - Redirect URI: {}", redirectUri);
        // redirect_uri가 없으면 기본값을 설정하거나 예외 처리
        if (redirectUri == null || redirectUri.isEmpty()) {
            redirectUri = "http://localhost:3000";  // 예시로 기본 프론트엔드 URL 설정
        }
        // 리디렉션할 URL에 accessToken과 refreshToken 추가
        String finalRedirectUri = String.format("%s?accessToken=%s&refreshToken=%s",
                redirectUri, tokenDto.getAccessToken(), tokenDto.getRefreshToken());
        getRedirectStrategy().sendRedirect(request, response, finalRedirectUri);
    }
}