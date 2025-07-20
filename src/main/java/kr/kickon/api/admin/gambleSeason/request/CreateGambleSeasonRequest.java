package kr.kickon.api.admin.gambleSeason.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;

@Data
@Schema(description = "승부 예측 시즌 생성 요청 DTO")
public class CreateGambleSeasonRequest {

  @Schema(description = "리그Pk", example = ExampleConstants.leaguePk)
  private Long leaguePk;

  @Schema(description = "제목", example = ExampleConstants.seasonTitle)
  private String title;

  @NotNull
  @Schema(description = "시작일", example = ExampleConstants.startDatetime)
  private String startedAt;

  @NotNull
  @Schema(description = "마감일", example = ExampleConstants.endDatetime)
  private String finishedAt;

  @Schema(description = "설명(비고)", example = ExampleConstants.etc)
  private String description;

}
