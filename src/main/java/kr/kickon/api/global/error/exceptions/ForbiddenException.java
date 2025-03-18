package kr.kickon.api.global.error.exceptions;

import kr.kickon.api.global.common.enums.ResponseCode;

public class ForbiddenException extends BaseException {
    public ForbiddenException(ResponseCode responseCode) {
        super(responseCode);
    }
}
