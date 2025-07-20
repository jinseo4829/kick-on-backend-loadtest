package kr.kickon.api.admin.actualSeason.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;

@Data
@Schema(description = "시즌 수정 요청 DTO")
public class UpdateActualSeasonRequest {
  @Schema(description = "리그Pk", example = ExampleConstants.leaguePk)
  private Long leaguePk;

  @Schema(description = "제목", example = ExampleConstants.seasonTitle)
  private String title;

  @Schema(description = "시작일", example = ExampleConstants.startDatetime)
  private String startedAt;

  @Schema(description = "마감일", example = ExampleConstants.endDatetime)
  private String finishedAt;

  @Schema(description = "상태", example = ExampleConstants.operatingStatus)
  private String operatingStatus;

  @Schema(description = "설명(비고)", example = ExampleConstants.etc)
  private String description;

  @Schema(description = "연도", example = ExampleConstants.year)
  private Integer year;

  @Schema(description = "참여 팀 리스트", example = ExampleConstants.seasonTeams)
  private List<Long> actualSeasonTeams;

}
