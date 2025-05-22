package kr.kickon.api.admin.game.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import kr.kickon.api.global.common.enums.GameStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "게임 수정 요청 DTO")
public class GameUpdateRequest {

    @Schema(description = "경기 시작 시간", example = "2025-10-18T05:00:00")
    @FutureOrPresent(message = "경기 시작 시간은 현재 이후여야 합니다.")
    private LocalDateTime startedAt;

    @Schema(description = "게임 상태 (예: PENDING, FINISHED, CANCELED)", example = "FINISHED")
    private GameStatus gameStatus;

    @Schema(description = "라운드", example = "Regular Season - 34")
    @Size(max = 100, message = "라운드는 100자 이하로 입력해주세요.")
    private String round;

    @Schema(description = "API ID", example = "1340894")
    @Positive(message = "API ID는 양수여야 합니다.")
    private Long apiId;

    @Schema(description = "홈팀 점수", example = "2")
    @Min(value = 0, message = "점수는 0 이상이어야 합니다.")
    private Integer homeScore;

    @Schema(description = "원정팀 점수", example = "1")
    @Min(value = 0, message = "점수는 0 이상이어야 합니다.")
    private Integer awayScore;

    @Schema(description = "홈팀 PK 점수", example = "5")
    @Min(value = 0, message = "PK 점수는 0 이상이어야 합니다.")
    private Integer homePenaltyScore;

    @Schema(description = "원정팀 PK 점수", example = "4")
    @Min(value = 0, message = "PK 점수는 0 이상이어야 합니다.")
    private Integer awayPenaltyScore;
}