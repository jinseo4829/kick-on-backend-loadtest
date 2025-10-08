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
        TokenDto tokenDto = jwtTokenProvider.createToken(authentication);

        // 쿠키에 토큰 설정
        jwtTokenProvider.setTokenCookies(response, tokenDto);

        // 클라이언트에서 전달한 redirect_uri 파라미터를 얻기
        String redirectUri = request.getParameter("state");
//        log.info("OAuth2 Success - Redirect URI: {}", redirectUri);
        // redirect_uri가 없으면 기본값을 설정하거나 예외 처리
        if (redirectUri == null || redirectUri.isEmpty()) {
            redirectUri = "http://localhost:3000";  // 예시로 기본 프론트엔드 URL 설정
        }
        // 리디렉션할 URL에 accessToken과 refreshToken 추가
        // 토큰 없이 리디렉션 (쿠키에 이미 설정됨)
        //String finalRedirectUri = String.format("%s?accessToken=%s&refreshToken=%s",
        //        redirectUri, tokenDto.getAccessToken(), tokenDto.getRefreshToken());
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
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