package kr.kickon.api.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SeasonTeamDTO {

  @Schema(description = "팀 고유 pk", example = "1")
  private Long pk;

  @Schema(description = "한글 이름", example = ExampleConstants.teamKrName)
  private String nameKr;

  @Schema(description = "영어 이름", example = ExampleConstants.teamnameEn)
  private String nameEn;

  @Schema(description = "팀 url", example = ExampleConstants.teamLogoUrl)
  private String logoUrl;

  public SeasonTeamDTO(Team team) {
    this.pk = team.getPk();
    this.nameKr = team.getNameKr();
    this.nameEn = team.getNameEn();
    this.logoUrl = team.getLogoUrl();
  }
}
