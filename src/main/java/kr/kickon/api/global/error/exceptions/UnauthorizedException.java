package kr.kickon.api.global.error.exceptions;

import kr.kickon.api.global.common.enums.ResponseCode;
import org.springframework.security.core.AuthenticationException;


public class UnauthorizedException extends BaseException {

    public UnauthorizedException(ResponseCode responseCode) {
        super(responseCode);
    }

    public AuthenticationException toAuthenticationException() {
        return new AuthenticationException(getMessage()) {}; // 익명 클래스로 변환
    }
}
