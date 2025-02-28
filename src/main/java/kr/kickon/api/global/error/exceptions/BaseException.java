package kr.kickon.api.global.error.exceptions;

import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.config.SpringContext;
import kr.kickon.api.global.util.slack.SlackService;
import lombok.Getter;
import java.util.Arrays;
@Getter
public class BaseException extends RuntimeException {
    private final ResponseCode responseCode;
    private final SlackService slackService;

    public BaseException(ResponseCode responseCode){
        super(responseCode.getMessage());
        this.slackService = SpringContext.getBean(SlackService.class);
        slackService.sendErrorMessage(responseCode.getMessage(), Arrays.toString(super.getStackTrace()));
        this.responseCode = responseCode;
    }

    public BaseException(ResponseCode responseCode, String message) {
        super(responseCode.getMessage() + ": " + message);
        this.slackService = SpringContext.getBean(SlackService.class);
        slackService.sendErrorMessage(responseCode.getMessage() + ": " + message, Arrays.toString(super.getStackTrace()));
        this.responseCode = responseCode;
    }

    public int getHttpStatus() {
        return responseCode.getHttpStatus().value();
    }
}