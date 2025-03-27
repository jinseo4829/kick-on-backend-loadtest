package kr.kickon.api.global.error.exceptions;

import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.config.SpringContext;
import kr.kickon.api.global.util.slack.SlackService;

import java.util.Arrays;

public class InternalServerException extends BaseException{
    private final SlackService slackService;
    public InternalServerException(ResponseCode responseCode) {
        super(responseCode);

        this.slackService = SpringContext.getBean(SlackService.class);
        slackService.sendErrorMessage(responseCode.getMessage() + ": " + responseCode.getMessage(), Arrays.toString(super.getStackTrace()));
    }

    public InternalServerException(ResponseCode responseCode, String message) {
        super(responseCode, message);
        this.slackService =  SpringContext.getBean(SlackService.class);
        slackService.sendErrorMessage(responseCode.getMessage() + ": " + message, Arrays.toString(super.getStackTrace()));
    }
}
