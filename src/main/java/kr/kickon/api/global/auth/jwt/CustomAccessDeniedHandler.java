package kr.kickon.api.global.auth.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.enums.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        log.error("CustomAuthenticationEntryPoint 호출됨: {}", accessDeniedException.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value()); // 403 상태 코드
        response.setContentType("application/json; charset=UTF-8");

        ResponseDTO<String> errorResponse = ResponseDTO.error(ResponseCode.FORBIDDEN, accessDeniedException.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
//        response.getWriter().flush();
    }
}
