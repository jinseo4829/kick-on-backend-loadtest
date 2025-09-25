package kr.kickon.api.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateChatRoomRequest {

    @NotNull(message = "팀은 필수값입니다.")
    @Schema(description = "팀 pk", example = ExampleConstants.teamPk)
    private Long teamPk;

    @Schema(description = "채팅방 이름", example = ExampleConstants.chatRoomTitle)
    private String roomTitle;

    @Schema(description = "채팅방 열리는 시각", example = ExampleConstants.startDatetime)
    private LocalDateTime openTime;

    @Schema(description = "채팅방 닫히는 시각", example = ExampleConstants.endDatetime)
    private LocalDateTime closeTime;
}