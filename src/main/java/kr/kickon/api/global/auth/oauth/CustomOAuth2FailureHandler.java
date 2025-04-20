package kr.kickon.api.global.auth.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value()); // 탈퇴 유저라면 403
        response.setContentType("application/json; charset=UTF-8");

        String message = "소셜 로그인에 실패했습니다.";
        if (exception instanceof OAuth2AuthenticationException oAuth2Exception) {
            message = oAuth2Exception.getError().getDescription(); // 우리가 던졌던 메시지
        }

        ResponseDTO<String> errorResponse = ResponseDTO.error(ResponseCode.FORBIDDEN_RESISTER, message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}