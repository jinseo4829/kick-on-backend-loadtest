package kr.kickon.api.global.error.exceptions;

import kr.kickon.api.global.common.enums.ResponseCode;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;


public class OAuth2RegistrationException extends OAuth2AuthenticationException {

    public OAuth2RegistrationException(ResponseCode responseCode) {
        super(new OAuth2Error(responseCode.getCode(), responseCode.getMessage(), null));
    }
}