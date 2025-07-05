package kr.kickon.api.admin.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.Team;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@Schema(description = "팀 리스트 응답 DTO")
public class TeamListDTO {

  @Schema(description = "팀 고유 PK", example = "1647")
  private Long pk;

  @Schema(description = "리그")
  private LeagueDTO league;

  @Schema(description = "한글 이름", example = ExampleConstants.teamKrName)
  private String nameKr;

  @Schema(description = "영어 이름", example = ExampleConstants.teamnameEn)
  private String nameEn;

  @Schema(description = "팀 url", example = ExampleConstants.teamLogoUrl)
  private String logoUrl;

  @Schema(description = "진행 중 시즌PK", example = "1")
  private Long seasonPk;

  @Schema(description = "진행 중 시즌 title", example = "24/25 K 리그")
  private String seasonTitle;

  public static kr.kickon.api.admin.team.dto.TeamListDTO fromEntity(
      Team team) {

    return TeamListDTO.builder()
        .pk(team.getPk())
        .nameKr(team.getNameKr())
        .nameEn(team.getNameEn())
        .logoUrl(team.getLogoUrl())
        .build();
  }
}
