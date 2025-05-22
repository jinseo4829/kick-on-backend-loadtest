package kr.kickon.api.admin.game.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.enums.GameStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "게임 상세 조회 응답 DTO")
public class GameDetailDTO {

    @Schema(description = "게임 PK", example = "1564")
    private Long pk;

    @Schema(description = "게임 UUID", example = "9e9aabd6-6f40-4842-9bdd-1013cab55680")
    private String id;

    @Schema(description = "API ID (외부 API 연동용)", example = "1340894")
    private Long apiId;

    @Schema(description = "게임 상태", example = "PENDING")
    private GameStatus gameStatus;

    @Schema(description = "경기 시작 시간", example = "2025-10-18T05:00:00")
    private LocalDateTime startedAt;

    @Schema(description = "홈 팀 점수", nullable = true, example = "1")
    private Integer homeScore;

    @Schema(description = "어웨이 팀 점수", nullable = true, example = "2")
    private Integer awayScore;

    @Schema(description = "홈 팀 페널티 점수", nullable = true, example = "3")
    private Integer homePenaltyScore;

    @Schema(description = "어웨이 팀 페널티 점수", nullable = true, example = "2")
    private Integer awayPenaltyScore;

    @Schema(description = "라운드 정보", example = "Regular Season - 33")
    private String round;

    @Schema(description = "기타 정보", nullable = true)
    private String etc;

    @Schema(description = "홈 팀 정보")
    private GameListDTO.TeamSummary homeTeam;

    @Schema(description = "어웨이 팀 정보")
    private GameListDTO.TeamSummary awayTeam;

    @Schema(description = "실제 시즌 정보")
    private GameListDTO.ActualSeasonSummary actualSeason;
}