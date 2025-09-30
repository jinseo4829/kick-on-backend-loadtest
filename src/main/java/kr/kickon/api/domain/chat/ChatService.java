package kr.kickon.api.domain.chat;

import kr.kickon.api.domain.chat.request.CreateChatRoomRequest;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class ChatService {
    private final Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    // 채팅방 생성
    public ChatRoom createChatRoom(CreateChatRoomRequest request) {
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

        return chatRoom;
    }

    // 방 즉시 열기
    public ChatRoom openRoom(String roomId) {
        ChatRoom room = chatRooms.get(roomId);
        if (room != null) {
            room.setOpen(true);
        }else {
            throw new NotFoundException(ResponseCode.NOT_FOUND_CHATROOM);
        }
        return room;
    }

    // 방 즉시 닫기
    public ChatRoom closeRoom(String roomId) {
        ChatRoom room = chatRooms.get(roomId);
        if (room != null) {
            room.setOpen(false);
        }else {
            throw new NotFoundException(ResponseCode.NOT_FOUND_CHATROOM);
        }
        return room;
    }

    // 스케줄된 방 열기
    private void scheduleRoomOpen(String roomId, LocalDateTime openTime) {
        long delaySeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), openTime);
        if (delaySeconds > 0) {
            scheduler.schedule(() -> {
                openRoom(roomId);
                System.out.println("채팅방 열림: " + roomId);
            }, delaySeconds, java.util.concurrent.TimeUnit.SECONDS);
        } else {
            // 이미 지난 시간이면 바로 열기
            openRoom(roomId);
        }
    }

    // 스케줄된 방 닫기
    private void scheduleRoomClose(String roomId, LocalDateTime closeTime) {
        long delaySeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), closeTime);
        if (delaySeconds > 0) {
            scheduler.schedule(() -> {
                closeRoom(roomId);
                System.out.println("채팅방 닫힘: " + roomId);
            }, delaySeconds, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    // 방이 열려있는지 확인
    public boolean isRoomOpen(String roomId) {
        ChatRoom room = chatRooms.get(roomId);
        return room != null && room.isOpen();
    }

    // 특정 방 정보 조회
    public ChatRoom getChatRoom(String roomId) {
        return chatRooms.get(roomId);
    }

    // 모든 방 목록 조회
    public List<ChatRoom> getAllChatRooms() {
        return new ArrayList<>(chatRooms.values());
    }

    // 현재 열린 방 목록 조회
    public List<ChatRoom> getOpenChatRooms() {
        return chatRooms.values().stream()
                .filter(ChatRoom::isOpen)
                .toList();
    }

    // 특정 팀의 방 목록 조회
    public List<ChatRoom> getChatRoomsByTeam(Long teamPk) {
        return chatRooms.values().stream()
                .filter(room -> room.getTeamPk().equals(teamPk))
                .toList();
    }

    // 방 삭제
    public boolean deleteChatRoom(String roomId) {
        ChatRoom removed = chatRooms.remove(roomId);
        return removed != null;
    }

}