package kr.kickon.api.admin.actualSeason.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.kickon.api.admin.gambleSeason.dto.SeasonListDTO;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@Schema(description = "실제 시즌 상세 응답 DTO")
public class ActualSeasonDetailDTO extends SeasonListDTO {

  @Schema(description = "설명(비고)", example = ExampleConstants.etc)
  private String description;

  @Schema(description = "연도", example = ExampleConstants.year)
  private int year;
  
  @Schema(description = "참여 팀 리스트")
  private List<SeasonTeamDTO> actualSeasonTeams;

  public static ActualSeasonDetailDTO fromEntity(
      ActualSeason season,
      List<SeasonTeamDTO> seasonTeamList){
    LeagueDTO leagueDto = null;
    if (season.getLeague() != null) {
      leagueDto = new LeagueDTO(season.getLeague());
    }

    return ActualSeasonDetailDTO.builder()
        .pk(season.getPk())
        .title(season.getTitle())
        .startedAt(season.getStartedAt().atStartOfDay())
        .finishedAt(season.getFinishedAt().atStartOfDay())
        .operatingStatus(season.getOperatingStatus())
        .description(season.getDescription())
        .league(leagueDto)
        .year(season.getYear())
        .actualSeasonTeams(seasonTeamList)
        .build();
  }
}
