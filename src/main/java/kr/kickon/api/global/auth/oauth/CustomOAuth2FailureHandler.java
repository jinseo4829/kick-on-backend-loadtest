package kr.kickon.api.global.auth.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kickon.api.global.auth.jwt.dto.TokenDto;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class CustomOAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String errorCode = "INVALID";
        if (exception instanceof OAuth2AuthenticationException oAuth2Exception) {
            errorCode = oAuth2Exception.getError().getErrorCode(); // 우리가 던졌던 메시지
        }
        // 클라이언트에서 전달한 redirect_uri 파라미터를 얻기
        String redirectUri = request.getParameter("state");
        if (redirectUri == null || redirectUri.isEmpty()) {
            redirectUri = "http://localhost:3000";  // 예시로 기본 프론트엔드 URL 설정
        }
        String encodedMessage = URLEncoder.encode(errorCode, StandardCharsets.UTF_8);
        // 리디렉션할 URL에 accessToken과 refreshToken 추가
        String finalRedirectUri = String.format("%s?errorCode=%s",
                redirectUri, encodedMessage);
        getRedirectStrategy().sendRedirect(request, response, finalRedirectUri);
    }
}