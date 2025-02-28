package kr.kickon.api.global.error.exceptions;

import kr.kickon.api.global.common.enums.ResponseCode;

public class NotFoundException extends BaseException {
    public NotFoundException(ResponseCode responseCode) {
        super(responseCode);
    }
    public NotFoundException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }
}
