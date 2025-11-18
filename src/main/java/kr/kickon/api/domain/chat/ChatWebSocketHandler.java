package kr.kickon.api.domain.chat;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;

    public ChatWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        String roomId = getRoomIdFromSession(session);
        chatService.registerSession(roomId, session);
        System.out.println("WebSocket connected: " + roomId + ", sessionId=" + session.getId());
        System.out.println("현재 세션 수: " + chatService.getSessionCount(roomId));
    }

    @Override
    public void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
        String roomId = getRoomIdFromSession(session);
        System.out.println("받은 메시지 from session " + session.getId() + ": " + message.getPayload());

        chatService.broadcastMessage(roomId, message.getPayload());

        System.out.println("브로드캐스트 완료 for room " + roomId);
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        String roomId = getRoomIdFromSession(session);
        chatService.removeSession(roomId, session);
        System.out.println("WebSocket disconnected: " + roomId + ", sessionId=" + session.getId());
        System.out.println("현재 세션 수: " + chatService.getSessionCount(roomId));
    }

    private String getRoomIdFromSession(WebSocketSession session) {
        // URL 예: ws://localhost:8080/chat-ws?roomId=123
        return Objects.requireNonNull(session.getUri()).getQuery().split("=")[1];
    }
}
