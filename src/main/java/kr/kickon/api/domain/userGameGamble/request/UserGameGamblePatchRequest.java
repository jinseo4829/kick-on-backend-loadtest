package kr.kickon.api.domain.userGameGamble.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PositiveOrZero;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Getter;

@Getter
public class UserGameGamblePatchRequest {
    @NotNull(message = "승부예측 ID는 필수 요소입니다.")
    @Schema(example = ExampleConstants.id, description = "승부예측 ID")
    private String gamble;

    @NotNull(message = "홈 점수 예측값은 필수 요소입니다.")
    @PositiveOrZero(message = "홈 점수 예측값은 0혹은 양수입니다.")
    @Schema(example = "1", description = "홈 점수 예측값")
    private Integer predictedHomeScore;

    @NotNull(message = "어웨이 점수 예측값은 필수 요소입니다.")
    @PositiveOrZero(message = "어웨이 점수 예측값은 0혹은 양수입니다.")
    @Schema(example = "1", description = "어웨이 점수 예측값")
    private Integer predictedAwayScore;
}