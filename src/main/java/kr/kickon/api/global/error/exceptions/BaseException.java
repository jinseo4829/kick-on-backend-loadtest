package kr.kickon.api.global.error.exceptions;

import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.config.SpringContext;
import kr.kickon.api.global.util.slack.SlackService;
import lombok.Getter;
import java.util.Arrays;
@Getter
public class BaseException extends RuntimeException {
    private final ResponseCode responseCode;

    public BaseException(ResponseCode responseCode){
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public BaseException(ResponseCode responseCode, String message) {
        super(responseCode.getMessage() + ": " + message);
        this.responseCode = responseCode;
    }

    public BaseException(ResponseCode responseCode, Throwable cause) {
        super(responseCode.getMessage(), cause);
        this.responseCode = responseCode;
    }

    public int getHttpStatus() {
        return responseCode.getHttpStatus().value();
    }
}