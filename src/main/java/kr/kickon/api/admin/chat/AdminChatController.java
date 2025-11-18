package kr.kickon.api.admin.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.chat.ChatService;
import kr.kickon.api.domain.chat.dto.ChatRoomDTO;
import kr.kickon.api.domain.chat.request.CreateChatRoomRequest;
import kr.kickon.api.domain.chat.response.GetChatRoomDetailResponse;
import kr.kickon.api.domain.chat.response.GetChatRoomsResponse;
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
                    content = @Content(schema = @Schema(implementation = GetChatRoomDetailResponse.class))),
    })
    @PostMapping
    public ResponseEntity<ResponseDTO<ChatRoomDTO>> createChatRoom(@RequestBody CreateChatRoomRequest request) {
        ChatRoomDTO chatRoom = chatService.createChatRoom(request);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, chatRoom));
    }

    @Operation(summary = "채팅방 즉시 열기", description = "특정 채팅방 즉시 열기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetChatRoomDetailResponse.class))),
    })
    @PostMapping("/{roomId}/open")
    public ResponseEntity<ResponseDTO<ChatRoomDTO>> openRoom(@PathVariable String roomId) {
        ChatRoomDTO chatRoom = chatService.openRoom(roomId);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, chatRoom));
    }

    @Operation(summary = "채팅방 즉시 닫기", description = "특정 채팅방 즉시 닫기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetChatRoomDetailResponse.class))),
    })
    @PostMapping("/{roomId}/close")
    public ResponseEntity<ResponseDTO<ChatRoomDTO>> closeRoom(@PathVariable String roomId) {
        ChatRoomDTO chatRoom = chatService.closeRoom(roomId);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, chatRoom));
    }

    @Operation(summary = "채팅방 목록 조회", description = "모든 채팅방 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetChatRoomsResponse.class))),
    })
    @GetMapping
    public List<ChatRoomDTO> getAllChatRooms() {
        return chatService.getAllChatRooms();
    }

    @Operation(summary = "open 채팅방 목록 조회", description = "현재 open된 채팅방 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetChatRoomsResponse.class))),
    })
    @GetMapping("/open")
    public List<ChatRoomDTO> getOpenChatRooms() {
        return chatService.getOpenChatRooms();
    }

    @Operation(summary = "특정 팀 채팅방 목록 조회", description = "특정 팀 pk로 채팅방 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetChatRoomsResponse.class))),
    })    @GetMapping("/team/{teamName}")
    public List<ChatRoomDTO> getChatRoomsByTeam(@PathVariable Long teamPk) {
        return chatService.getChatRoomsByTeam(teamPk);
    }

    @Operation(summary = "채팅방 상세 조회", description = "roomId로 채팅방 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetChatRoomDetailResponse.class))),
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDTO> getChatRoom(@PathVariable String roomId) {
        ChatRoomDTO chatRoom = chatService.getChatRoom(roomId);
        if (chatRoom != null) {
            return ResponseEntity.ok(chatRoom);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "채팅방 삭제", description = "roomId로 채팅방 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
    })
    @DeleteMapping("/{roomId}")
    public ResponseEntity<String> deleteChatRoom(@PathVariable String roomId) {
        if (chatService.deleteChatRoom(roomId)) {
            return ResponseEntity.ok("채팅방이 삭제되었습니다.");
        }
        return ResponseEntity.notFound().build();
    }
}
