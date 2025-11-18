package kr.kickon.api.domain.chat;

import kr.kickon.api.domain.chat.dto.ChatRoomDTO;
import kr.kickon.api.domain.chat.request.CreateChatRoomRequest;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

@Service
public class ChatService {
    private final Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();
    // 방별 WebSocket 세션 관리
    private final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    // 채팅방 생성
    public ChatRoomDTO createChatRoom(CreateChatRoomRequest request) {
        String roomId = UUID.randomUUID().toString();

        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .teamPk(request.getTeamPk())
                .roomTitle(request.getRoomTitle())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .isOpen(false)
                .createdAt(LocalDateTime.now())
                .build();

        chatRooms.put(roomId, chatRoom);

        // 자동으로 열리고 닫히도록 스케줄링
        scheduleRoomOpen(roomId, request.getOpenTime());
        scheduleRoomClose(roomId, request.getCloseTime());

        return new ChatRoomDTO(chatRoom);
    }

    // 방 즉시 열기
    public ChatRoomDTO openRoom(String roomId) {
        ChatRoom room = chatRooms.get(roomId);
        if (room == null) {
            throw new NotFoundException(ResponseCode.NOT_FOUND_CHATROOM);
        }
        room.setOpen(true);
        return new ChatRoomDTO(room);
    }

    // 방 즉시 닫기
    public ChatRoomDTO closeRoom(String roomId) {
        ChatRoom room = chatRooms.get(roomId);
        if (room == null) {
            throw new NotFoundException(ResponseCode.NOT_FOUND_CHATROOM);
        }
        room.setOpen(false);
        return new ChatRoomDTO(room);
    }

    // 스케줄된 방 열기
    private void scheduleRoomOpen(String roomId, LocalDateTime openTime) {
        long delaySeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), openTime);
        if (delaySeconds > 0) {
            scheduler.schedule(() -> openRoom(roomId), delaySeconds, TimeUnit.SECONDS);
        } else openRoom(roomId);
    }

    // 스케줄된 방 닫기
    private void scheduleRoomClose(String roomId, LocalDateTime closeTime) {
        long delaySeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), closeTime);
        if (delaySeconds > 0) {
            scheduler.schedule(() -> closeRoom(roomId), delaySeconds, TimeUnit.SECONDS);
        }
    }

    // 방이 열려있는지 확인
    public boolean isRoomOpen(String roomId) {
        ChatRoom room = chatRooms.get(roomId);
        return room != null && room.isOpen();
    }

    // WebSocket 세션 등록
    public void registerSession(String roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    // WebSocket 세션 제거
    public void removeSession(String roomId, WebSocketSession session) {
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) sessions.remove(session);
    }

    // 메시지 브로드캐스트
    public void broadcastMessage(String roomId, String message) {
        ChatRoom room = chatRooms.get(roomId);
        if (room == null || !room.isOpen()) return; // 방이 닫혀있으면 무시

        List<WebSocketSession> sessions = roomSessions.getOrDefault(roomId, List.of());
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // 특정 방 정보 조회
    public ChatRoomDTO getChatRoom(String roomId) {return new ChatRoomDTO(chatRooms.get(roomId));
    }

    // 모든 방 목록 조회
    public List<ChatRoomDTO> getAllChatRooms() {
        return chatRooms.values().stream()
                .map(ChatRoomDTO::new)
                .toList();
    }

    // 현재 열린 방 목록 조회
    public List<ChatRoomDTO> getOpenChatRooms() {
        return chatRooms.values().stream()
                .filter(ChatRoom::isOpen)
                .map(ChatRoomDTO::new)
                .toList();
    }

    // 특정 팀의 방 목록 조회
    public List<ChatRoomDTO> getChatRoomsByTeam(Long teamPk) {
        return chatRooms.values().stream()
                .filter(room -> room.getTeamPk().equals(teamPk))
                .map(ChatRoomDTO::new)
                .toList();
    }

    // 방 삭제
    public boolean deleteChatRoom(String roomId) {
        ChatRoom removed = chatRooms.remove(roomId);
        roomSessions.remove(roomId); // 세션 제거
        return removed != null;
    }

    public int getSessionCount(String roomId) {
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        return sessions == null ? 0 : sessions.size();
    }

}