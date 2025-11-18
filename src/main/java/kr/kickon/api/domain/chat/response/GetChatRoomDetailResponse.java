package kr.kickon.api.domain.chat.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.chat.dto.ChatRoomDTO;
import kr.kickon.api.global.common.ResponseDTO;

@Schema(description = "채팅방 상세 조회 응답")
public class GetChatRoomDetailResponse extends ResponseDTO<ChatRoomDTO> {
}
