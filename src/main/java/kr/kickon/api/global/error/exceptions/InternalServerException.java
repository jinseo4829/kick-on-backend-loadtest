package kr.kickon.api.global.error.exceptions;

import kr.kickon.api.global.common.enums.ResponseCode;

public class InternalServerException extends BaseException{

    public InternalServerException(ResponseCode responseCode) {
        super(responseCode);
    }

    public InternalServerException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }
}
