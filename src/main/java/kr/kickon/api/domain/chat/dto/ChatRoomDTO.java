package kr.kickon.api.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.chat.ChatRoom;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Schema(description = "채팅방 조회를 위한 채팅방 DTO")
public class ChatRoomDTO {

    @Schema(description = "채팅방 id", example = ExampleConstants.id)
    private String id;

    @Schema(description = "팀 pk", example = ExampleConstants.teamPk)
    private Long teamPk;

    @Schema(description = "채팅방 이름", example = ExampleConstants.chatRoomTitle)
    private String roomTitle;

    @Schema(description = "채팅방 열리는 시각", example = ExampleConstants.startDatetime)
    private LocalDateTime openTime;

    @Schema(description = "채팅방 닫히는 시각", example = ExampleConstants.endDatetime)
    private LocalDateTime closeTime;

    @Schema(description = "채팅방 열림 여부", example = "true")
    private Boolean isOpen;

    @Schema(description = "채팅방 생성 시각", example = ExampleConstants.datetime)
    private LocalDateTime createdAt;

    public ChatRoomDTO(ChatRoom room) {
        this.id = room.getRoomId();
        this.teamPk = room.getTeamPk();
        this.roomTitle = room.getRoomTitle();
        this.openTime = room.getOpenTime();
        this.closeTime = room.getCloseTime();
        this.isOpen = room.isOpen();
        this.createdAt = getCreatedAt();
    }
}
