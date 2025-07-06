package kr.kickon.api.domain.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "내 예측 통계 DTO")
public class MyPredictionStatsResponse {
    @Schema(description = "누적 성공률 (0.75 -> 75%)")
    private double totalSuccessRate;

    @Schema(description = "총 참여 횟수")
    private int totalParticipationCount;

    @Schema(description = "내 응원팀 경기 중 예측 참여율 (0.5 -> 50%)")
    private double participationRate;

    @Schema(description = "이번 달 성공률 (0.6 -> 60%)")
    private double thisMonthSuccessRate;

    @Schema(description = "이번 달 맞춘 개수 / 참여 개수 (예: 2/4)")
    private String thisMonthHitSummary;

    @Schema(description = "이번 달 누적 포인트")
    private int thisMonthPoints;

    @Schema(description = "총 누적 포인트")
    private int totalPoints;

    @Schema(description = "가장 많이 적중한 응원팀 이름")
    private String mostHitTeamName;
}

