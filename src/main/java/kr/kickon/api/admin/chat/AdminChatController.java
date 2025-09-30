package kr.kickon.api.admin.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.admin.board.response.AdminGetBoardDetailResponse;
import kr.kickon.api.domain.chat.ChatService;
import kr.kickon.api.domain.chat.ChatRoom;
import kr.kickon.api.domain.chat.request.CreateChatRoomRequest;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.enums.ResponseCode;
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

    @Operation(summary = "채팅방 생성", description = "특정 팀 채팅방 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ChatRoom.class))),
    })
    @PostMapping
    public ResponseEntity<ResponseDTO<ChatRoom>> createChatRoom(@RequestBody CreateChatRoomRequest request) {
        ChatRoom chatRoom = chatService.createChatRoom(request);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, chatRoom));
    }

    @Operation(summary = "채팅방 즉시 열기", description = "특정 채팅방 즉시 열기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ChatRoom.class))),
    })
    @PostMapping("/{roomId}/open")
    public ResponseEntity<ResponseDTO<ChatRoom>> openRoom(@PathVariable String roomId) {
        ChatRoom chatRoom = chatService.openRoom(roomId);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, chatRoom));
    }

    @Operation(summary = "채팅방 즉시 닫기", description = "특정 채팅방 즉시 닫기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ChatRoom.class))),
    })      @PostMapping("/{roomId}/close")
    public ResponseEntity<ResponseDTO<ChatRoom>> closeRoom(@PathVariable String roomId) {
        ChatRoom chatRoom = chatService.closeRoom(roomId);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, chatRoom));
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
