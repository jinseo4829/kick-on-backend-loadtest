package kr.kickon.api.admin.gambleSeason.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "승부 예측 시즌 생성 요청 DTO")
public class PatchGambleSeasonRequestDTO {

  @Schema(description = "리그Pk", example = "1")
  private Long leaguePk;

  @Schema(description = "제목", example = "24/25 K 리그")
  private String title;

  @Schema(description = "시작일", example = "2025-03-16T12:00:00")
  private String startedAt;

  @Schema(description = "마감일", example = "2025-06-16T12:00:00")
  private String finishedAt;

  @Schema(description = "상태", example = "PROCEEDING")
  private String operatingStatus;

  @Schema(description = "설명(비고)")
  private String description;

  @Schema(description = "참여 팀 리스트", example = "[1647, 1648, 1649]")
  private List<Long> gambleSeasonTeams;
}
