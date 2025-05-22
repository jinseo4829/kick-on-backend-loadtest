package kr.kickon.api.global.auth.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.enums.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        log.error("CustomAuthenticationEntryPoint 호출됨: {}  / trace : {}", authException.getMessage(), authException.getStackTrace());
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 상태 코드
        response.setContentType("application/json; charset=UTF-8");

        ResponseDTO<String> errorResponse = ResponseDTO.error(ResponseCode.UNAUTHORIZED, authException.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}

