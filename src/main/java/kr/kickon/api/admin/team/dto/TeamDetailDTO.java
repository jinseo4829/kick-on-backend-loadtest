package kr.kickon.api.admin.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.entities.Team;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@Schema(description = "팀 상세 응답 DTO")
public class TeamDetailDTO extends TeamListDTO{

  @Schema(description = "진행 중 시즌 순위", example = "1")
  private Integer actualSeasonRankOrder;

  @Schema(description = "승부 예측 시즌 PK", example = "1")
  private Long gambleSeasonPk;

  @Schema(description = "승부 예측 시즌 title", example = "24/25 K 리그")
  private String gambleSeasonTitle;

  @Schema(description = "승부 예측 시즌 순위", example = "1")
  private Integer gambleSeasonRankOrder;

  public static TeamDetailDTO fromEntity(Team team) {
    return TeamDetailDTO.builder()
        .pk(team.getPk())
        .nameKr(team.getNameKr())
        .nameEn(team.getNameEn())
        .logoUrl(team.getLogoUrl())
        .build();
  }

}
