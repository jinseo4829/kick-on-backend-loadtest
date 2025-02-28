package kr.kickon.api.global.error.exceptions;

import kr.kickon.api.global.common.enums.ResponseCode;

public class BadRequestException extends BaseException {
    public BadRequestException(ResponseCode responseCode) {
        super(responseCode);
    }
}