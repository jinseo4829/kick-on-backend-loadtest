package kr.kickon.api.domain.actualSeasonRanking.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetActualSeasonRankingRequestDTO {
    @NotNull
    @Positive
    private long league;
}
