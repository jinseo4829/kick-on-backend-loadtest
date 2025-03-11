package kr.kickon.api.domain.userGameGamble.dto;

import kr.kickon.api.global.common.enums.PredictedResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GambleCountDTO {
    private PredictedResult predictedResult;
    private Long count;
}