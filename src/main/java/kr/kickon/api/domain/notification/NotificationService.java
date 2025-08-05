package kr.kickon.api.domain.notification;

import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.boardReply.BoardReplyService;
import kr.kickon.api.domain.notification.response.NotificationResponse;
import kr.kickon.api.global.common.entities.Notification;
import kr.kickon.api.global.common.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 알림 생성 + WebSocket 실시간 전송
     */
    public void sendNotification(User receiver, String type, String content, String redirectUrl) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .type(type)
                .content(content)
                .redirectUrl(redirectUrl)
                .read(false)
                .build();

        notificationRepository.save(notification);

        // 프론트로 실시간 전송
        messagingTemplate.convertAndSend(
                "/topic/notify/" + receiver.getPk(),
                NotificationResponse.from(notification)
        );

    }

    /**
     * 사용자 알림 목록 조회
     */
    public List<NotificationResponse> getNotifications(User user) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림이 존재하지 않습니다."));
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    public int getUnreadNotificationCount(User user) {
        return notificationRepository.countByReceiverAndReadIsFalse(user);
    }

}
