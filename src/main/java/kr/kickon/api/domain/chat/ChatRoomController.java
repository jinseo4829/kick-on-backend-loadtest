package kr.kickon.api.domain.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chatrooms")
@Tag(name = "게시글")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {
    private final ChatService chatService;

    @Operation(summary = "채팅방 리스트 조회", description = "사용자가 채팅방 리스트 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")})
    @GetMapping("/open")
    public List<ChatRoom> getOpenChatRooms() {
        return chatService.getOpenChatRooms();
    }

    @Operation(summary = "채팅방 정보 조회", description = "사용자가 채팅방 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")})    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getChatRoom(@PathVariable String roomId) {
        ChatRoom chatRoom = chatService.getChatRoom(roomId);
        if (chatRoom != null) {
            return ResponseEntity.ok(chatRoom);
        }
        return ResponseEntity.notFound().build();
    }
}
