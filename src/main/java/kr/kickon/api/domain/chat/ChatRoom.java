package kr.kickon.api.domain.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {

    @Schema(description = "채팅방 고유 id", example = ExampleConstants.id)
    private String roomId;

    @Schema(description = "팀 pk", example = ExampleConstants.teamPk)
    private Long teamPk;

    @Schema(description = "채팅방 이름", example = ExampleConstants.chatRoomTitle)
    private String roomTitle;

    @Schema(description = "채팅방 열리는 시각", example = ExampleConstants.startDatetime)
    private LocalDateTime openTime;

    @Schema(description = "채팅방 닫히는 시각", example = ExampleConstants.endDatetime)
    private LocalDateTime closeTime;

    @Schema(description = "현재 열려있는지 여부", example = "true")
    private boolean isOpen;

    @Schema(description = "채팅방 생성 시각", example = ExampleConstants.datetime)
    private LocalDateTime createdAt;
}