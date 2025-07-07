package kr.kickon.api.admin.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.Team;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
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

  @Schema(description = "진행 중 시즌 정보")
  private SeasonInfo actualSeason;

  public static kr.kickon.api.admin.team.dto.TeamListDTO fromEntity(
      Team team) {

    return TeamListDTO.builder()
        .pk(team.getPk())
        .nameKr(team.getNameKr())
        .nameEn(team.getNameEn())
        .logoUrl(team.getLogoUrl())
        .build();
  }
  @Data
  @SuperBuilder
  @Schema(description = "시즌 정보 DTO")
  public static class SeasonInfo {
    @Schema(description = "시즌 PK", example = "1")
    private Long pk;

    @Schema(description = "시즌 제목", example = "24/25 K 리그")
    private String title;

    @Schema(description = "순위", example = "1")
    private Integer rankOrder;
  }
}
