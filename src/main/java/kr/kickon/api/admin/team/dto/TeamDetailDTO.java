package kr.kickon.api.admin.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.admin.team.dto.TeamListDTO.SeasonInfo;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.Team;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@Schema(description = "팀 상세 응답 DTO")
public class TeamDetailDTO{

  @Schema(description = "팀 고유 PK", example = "1647")
  private Long pk;

  @Schema(description = "한글 이름", example = ExampleConstants.teamKrName)
  private String nameKr;

  @Schema(description = "영어 이름", example = ExampleConstants.teamnameEn)
  private String nameEn;

  @Schema(description = "팀 url", example = ExampleConstants.teamLogoUrl)
  private String logoUrl;

  @Schema(description = "진행 중 시즌 정보")
  private SeasonInfo actualSeason;

  @Schema(description = "승부 예측 시즌 정보")
  private SeasonInfo gambleSeason;

  @Schema(description = "팬 유저 수", example = ExampleConstants.fanCount)
  private Integer fanCount;

  @Schema(description = "api id", example = ExampleConstants.apiId)
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
