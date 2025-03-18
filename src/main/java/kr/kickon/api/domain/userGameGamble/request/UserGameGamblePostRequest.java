package kr.kickon.api.domain.userGameGamble.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Getter;

@Getter
public class UserGameGamblePostRequest {
    @NotNull(message = "매치 pk는 필수 요소입니다.")
    @Schema(example = "1", description = "매치 PK값")
    private Long game;

    @NotNull(message = "홈 점수 예측값은 필수 요소입니다.")
    @PositiveOrZero
    @Schema(example = "1", description = "홈 점수 예측값")
    private Integer predictedHomeScore;

    @NotNull(message = "어웨이 점수 예측값은 필수 요소입니다.")
    @PositiveOrZero
    @Schema(example = "1", description = "어웨이 점수 예측값")
    private Integer predictedAwayScore;
}