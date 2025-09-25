package kr.kickon.api.admin.chat;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.chat.ChatService;
import kr.kickon.api.domain.chat.ChatRoom;
import kr.kickon.api.domain.chat.dto.request.CreateChatRoomRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/chat")
@Tag(name = "채팅")
@Slf4j
public class AdminChatController {
    private final ChatService chatService;

    // 채팅방 생성
    @PostMapping
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody CreateChatRoomRequest request) {
        ChatRoom chatRoom = chatService.createChatRoom(
                request.getTeamPk(),
                request.getRoomTitle(),
                request.getOpenTime(),
                request.getCloseTime()
        );
        return ResponseEntity.ok(chatRoom);
    }

    // 방 즉시 열기
    @PostMapping("/{roomId}/open")
    public ResponseEntity<String> openRoom(@PathVariable String roomId) {
        if (chatService.openRoom(roomId)) {
            return ResponseEntity.ok("채팅방이 열렸습니다.");
        }
        return ResponseEntity.notFound().build();
    }

    // 방 즉시 닫기
    @PostMapping("/{roomId}/close")
    public ResponseEntity<String> closeRoom(@PathVariable String roomId) {
        if (chatService.closeRoom(roomId)) {
            return ResponseEntity.ok("채팅방이 닫혔습니다.");
        }
        return ResponseEntity.notFound().build();
    }

    // 모든 채팅방 목록
    @GetMapping
    public List<ChatRoom> getAllChatRooms() {
        return chatService.getAllChatRooms();
    }

    // 현재 열린 채팅방 목록
    @GetMapping("/open")
    public List<ChatRoom> getOpenChatRooms() {
        return chatService.getOpenChatRooms();
    }

    // 특정 팀 채팅방 목록
    @GetMapping("/team/{teamName}")
    public List<ChatRoom> getChatRoomsByTeam(@PathVariable Long teamPk) {
        return chatService.getChatRoomsByTeam(teamPk);
    }

    // 채팅방 상세 정보
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getChatRoom(@PathVariable String roomId) {
        ChatRoom chatRoom = chatService.getChatRoom(roomId);
        if (chatRoom != null) {
            return ResponseEntity.ok(chatRoom);
        }
        return ResponseEntity.notFound().build();
    }

    // 채팅방 삭제
    @DeleteMapping("/{roomId}")
    public ResponseEntity<String> deleteChatRoom(@PathVariable String roomId) {
        if (chatService.deleteChatRoom(roomId)) {
            return ResponseEntity.ok("채팅방이 삭제되었습니다.");
        }
        return ResponseEntity.notFound().build();
    }
}
