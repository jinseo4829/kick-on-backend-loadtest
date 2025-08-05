package kr.kickon.api.domain.notification;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.notification.response.NotificationCountResponse;
import kr.kickon.api.domain.notification.response.NotificationResponse;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "알림 관련")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    // region {getMyNotifications} 내 알림 목록 조회 API
    @Operation(
            summary = "내 알림 목록 조회",
            description = "로그인한 사용자의 전체 알림 목록을 최신순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<List<NotificationResponse>>> getMyNotifications() {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        List<NotificationResponse> notifications = notificationService.getNotifications(user);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, notifications));
    }
    // endregion

    // region {markAsRead} 알림 읽음 처리 API
    @Operation(
            summary = "알림 읽음 처리",
            description = "알림을 읽음 상태로 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<ResponseDTO<Void>> readNotification(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
    // endregion

    // region {getUnreadCount} 읽지 않은 알림 개수 조회 API
    @Operation(
            summary = "읽지 않은 알림 개수 조회",
            description = "읽지 않은 알림의 개수를 반환합니다. (알림 뱃지용)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = NotificationCountResponse.class)))
    })
    @GetMapping("/unread-count")
    public ResponseEntity<ResponseDTO<NotificationCountResponse>> getUnreadCount() {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        int count = notificationService.getUnreadNotificationCount(user);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, new NotificationCountResponse(count)));
    }
    // endregion

}
