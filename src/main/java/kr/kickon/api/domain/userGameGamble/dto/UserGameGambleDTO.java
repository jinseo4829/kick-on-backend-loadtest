package kr.kickon.api.domain.userGameGamble.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.entities.UserGameGamble;
import kr.kickon.api.global.common.enums.GambleStatus;
import kr.kickon.api.global.common.enums.PredictedResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "내 승부예측 DTO")
@AllArgsConstructor
public class UserGameGambleDTO {
    @Schema(description = "내 예측 ID", example = "53fd4230-615b-4a11-8a55-7c6ed833c641")
    private String id;
    @Schema(description = "예측한 홈 스코어", example = "1")
    private Integer homeScore;
    @Schema(description = "예측한 어웨이 스코어", example = "2")
    private Integer awayScore;
    @Schema(description = "예측한 결과", example = "HOME")
    private PredictedResult result;
    @Schema(description = "승부예측 상태값", example = "SUCCEED")
    private GambleStatus gambleStatus;

    public UserGameGambleDTO(UserGameGamble userGameGamble) {
        id = userGameGamble.getId();
        homeScore = userGameGamble.getPredictedHomeScore();
        awayScore = userGameGamble.getPredictedAwayScore();
        result = userGameGamble.getPredictedResult();
        gambleStatus = userGameGamble.getGambleStatus();
    }
}
