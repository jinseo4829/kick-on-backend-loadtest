package kr.kickon.api.admin.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonListDTO;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.GambleSeason;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "팀 리스트 응답 DTO")
public class TeamListDTO {

  @Schema(description = "팀 고유 PK", example = "1647")
  private Long pk;

  @Schema(description = "리그")
  private LeagueDTO league;

  @Schema(description = "팀 이름", example = ExampleConstants.teamName)
  private String teamName;

  @Schema(description = "진행 중 시즌", example = "PROCEEDING")
  private ActualSeason operatingStatus;

  public static kr.kickon.api.admin.gambleSeason.dto.GambleSeasonListDTO fromEntity(
      GambleSeason gambleSeason) {

    LeagueDTO leagueDto = null;
    if (gambleSeason.getActualSeason() != null &&
        gambleSeason.getActualSeason().getLeague() != null) {

      leagueDto = new LeagueDTO(gambleSeason.getActualSeason().getLeague());
    }

    return GambleSeasonListDTO.builder()
        .pk(gambleSeason.getPk())
        .title(gambleSeason.getTitle())
        .startedAt(gambleSeason.getStartedAt())
        .finishedAt(gambleSeason.getFinishedAt())
        .operatingStatus(gambleSeason.getOperatingStatus())
        .league(leagueDto)
        .build();
  }
}
