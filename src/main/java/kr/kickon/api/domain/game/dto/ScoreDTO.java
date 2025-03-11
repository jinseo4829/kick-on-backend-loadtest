package kr.kickon.api.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "득점 DTO")
public class ScoreDTO{
    @Schema(description = "홈 득점수", example = "2")
    private Integer homeScores;

    @Schema(description = "어웨이 득점수", example = "2")
    private Integer awayScores;

    @Schema(description = "홈 승부차기 득점수", example = "2")
    private Integer homePenaltyScores = null;

    @Schema(description = "어웨이 승부차기 득점수", example = "2")
    private Integer awayPenaltyScores = null;
}
