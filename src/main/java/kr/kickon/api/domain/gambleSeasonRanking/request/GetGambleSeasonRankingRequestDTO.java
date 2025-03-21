package kr.kickon.api.domain.gambleSeasonRanking.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetGambleSeasonRankingRequestDTO {
    @NotNull(message = "리그 pk 값은 필수값입니다.")
    @Positive(message = "리그 pk 양수값입니다.")
    @Schema(description = "리그 pk 값", example = "1")
    private long league;
}
