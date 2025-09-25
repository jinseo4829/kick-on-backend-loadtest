package kr.kickon.api.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
    public class ChatMessageDto {

    @Schema(description = "발신자 pk", example = ExampleConstants.userPk)
    private Long senderPk;

    @Schema(description = "채팅 내용", example = ExampleConstants.chatMessage)
    private String message;

    @Schema(description = "채팅 보낸 시각", example = ExampleConstants.datetime)
    private LocalDateTime timestamp;
    }
