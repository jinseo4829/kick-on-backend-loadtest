package kr.kickon.api.domain.notification;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.kickon.api.domain.notification.response.NotificationResponse;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements BaseService<Notification>  {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UUIDGenerator uuidGenerator;

    // region {findById} Notification UUID 기반 조회
    @Override
    public Notification findById(String uuid) {
        BooleanExpression predicate = QNotification.notification.id.eq(uuid).and(QNotification.notification.status.eq(DataStatus.ACTIVATED));
        Optional<Notification> notificationEntity = notificationRepository.findOne(predicate);
        return notificationEntity.orElse(null);
    }
    // endregion

    // region {findByPk} Notification PK 기반 조회
    @Override
    public Notification findByPk(Long pk) {
        BooleanExpression predicate = QNotification.notification.pk.eq(pk).and(QNotification.notification.status.eq(DataStatus.ACTIVATED));
        Optional<Notification> notificationEntity = notificationRepository.findOne(predicate);
        return notificationEntity.orElse(null);
    }
    // endregion

    /**
     * 알림 생성 + WebSocket 실시간 전송
     */

    public void sendNotification(User receiver,
                                 String type,
                                 String content,
                                 String redirectUrl) {
        sendNotification(receiver, type, content, redirectUrl, null); // teamLogo 없이
    }

    public void sendNotification(User receiver, String type, String content, String redirectUrl, String teamLogo) {
        String id = uuidGenerator.generateUniqueUUID(this::findById);
        Notification notification = Notification.builder()
                .id(id)
                .receiver(receiver)
                .type(type)
                .content(content)
                .redirectUrl(redirectUrl)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        String destination = "/topic/notify/" + receiver.getId();
        NotificationResponse payload = NotificationResponse.from(notification, teamLogo);

        // 로그 찍기
        log.info("알림 소켓 전송 → destination: {}, payload: {}", destination, payload);

        // 프론트로 실시간 전송 (한 번만)
        messagingTemplate.convertAndSend(destination, payload);
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
    public void markAsRead(Long notificationPk) {
        Notification notification = this.findByPk(notificationPk);
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    public int getUnreadNotificationCount(User user) {
        return notificationRepository.countByReceiverAndIsReadIsFalse(user);
    }

}
