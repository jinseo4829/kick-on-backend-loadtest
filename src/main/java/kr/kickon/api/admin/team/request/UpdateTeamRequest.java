package kr.kickon.api.admin.team.request;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;

@Data
@Schema(description = "시즌 수정 요청 DTO")
public class UpdateTeamRequest {

  @Schema(description = "한글 이름", example = ExampleConstants.teamKrName)
  private String nameKr;

  @Schema(description = "영어 이름", example = ExampleConstants.teamnameEn)
  private String nameEn;

  @Schema(description = "진행 중 시즌PK", example = "20")
  private Long actualSeasonPk;

  @Schema(description = "승부 예측 시즌 PK", example = "1")
  private Long gambleSeasonPk;

  @Schema(description = "팀 url", example = ExampleConstants.teamLogoUrl)
  private String logoUrl;
}
