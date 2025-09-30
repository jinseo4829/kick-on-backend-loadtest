package kr.kickon.api.domain.chat;

import kr.kickon.api.domain.chat.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessage(@DestinationVariable String roomId, ChatMessageDto messageDto) {
        if (!chatService.isRoomOpen(roomId)) {
            throw new IllegalStateException("채팅방이 닫혀 있습니다.");
        }

        messageDto.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, messageDto);
    }
}