package kr.kickon.api.admin.gambleSeason.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;

@Data
@Schema(description = "승부 예측 시즌 생성 요청 DTO")
public class UpdateGambleSeasonRequest extends CreateGambleSeasonRequest {

  @Schema(description = "상태", example = ExampleConstants.operatingStatus)
  private String operatingStatus;

  @Schema(description = "참여 팀 리스트", example = ExampleConstants.seasonTeams)
  private List<Long> gambleSeasonTeams;
}
