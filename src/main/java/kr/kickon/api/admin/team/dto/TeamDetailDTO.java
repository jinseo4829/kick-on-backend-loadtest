package kr.kickon.api.admin.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.entities.Team;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@Schema(description = "팀 상세 응답 DTO")
public class TeamDetailDTO extends TeamListDTO{

  @Schema(description = "승부 예측 시즌 정보")
  private SeasonInfo gambleSeason;

  @Schema(description = "팬 유저 수", example = "100")
  private Integer fanCount;

  @Schema(description = "api id", example = "2746")
  private Long apiId;

  public static TeamDetailDTO fromEntity(Team team) {
    return TeamDetailDTO.builder()
        .pk(team.getPk())
        .nameKr(team.getNameKr())
        .nameEn(team.getNameEn())
        .logoUrl(team.getLogoUrl())
        .apiId(team.getApiId())
        .build();
  }
}
