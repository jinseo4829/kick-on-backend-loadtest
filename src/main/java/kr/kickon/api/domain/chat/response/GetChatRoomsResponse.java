package kr.kickon.api.domain.chat.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.chat.dto.ChatRoomListDTO;
import kr.kickon.api.global.common.ResponseDTO;
import java.util.List;

@Schema(description = "채팅방 리스트 조회 응답")
public class GetChatRoomsResponse extends ResponseDTO<List<ChatRoomListDTO>> {
}
