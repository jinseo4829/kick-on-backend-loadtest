package kr.kickon.api.global.error.exceptions;

import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final ResponseCode responseCode;

    public BaseException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public int getHttpStatus() {
        return responseCode.getHttpStatus().value();
    }
}