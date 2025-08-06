package kr.kickon.api.domain.notification.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "NotificationCountResponse", description = "읽지 않은 알림 개수 응답")
public class NotificationCountResponse {

    @Schema(description = "읽지 않은 알림 수", example = "3")
    private int count;
}
