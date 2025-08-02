package kr.kickon.api.domain.notification.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.entities.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@Schema(name = "NotificationListResponse", description = "알림 목록 응답")
public class NotificationResponse {
    private Long pk;
    private String content;
    private String redirectUrl;
    private boolean read;
    private String relativeTime;
    private String absoluteTime;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .pk(notification.getPk())
                .content(notification.getContent())
                .redirectUrl(notification.getRedirectUrl())
                .read(notification.isRead())
                .relativeTime(toRelativeTime(notification.getCreatedAt()))
                .absoluteTime(notification.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm")))
                .build();
    }

    private static String toRelativeTime(LocalDateTime time) {
        Duration d = Duration.between(time, LocalDateTime.now());
        if (d.toMinutes() < 1) return "방금 전";
        if (d.toHours() < 1) return d.toMinutes() + "분 전";
        if (d.toDays() < 1) return d.toHours() + "시간 전";
        if (d.toDays() < 7) return d.toDays() + "일 전";
        return time.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"));
    }
}
