package kr.kickon.api.admin.gambleSeason.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@Schema(description = "승부 예측 시즌 상세 응답 DTO")
public class GambleSeasonDetailDTO extends GambleSeasonListDTO{

  @Schema(description = "설명(비고)", example = ExampleConstants.etc)
  private String description;

  @Schema(description = "참여 팀 리스트")
  private List<SeasonTeamDTO> gambleSeasonTeams;

  @Schema(description = "팀 순위")
  private List<GetGambleSeasonRankingDTO> gambleSeasonRanking;

  public static GambleSeasonDetailDTO fromEntity(
      GambleSeason gambleseason,
      List<SeasonTeamDTO> seasonTeamList,
      List<GetGambleSeasonRankingDTO> rankingList
  ) {
    LeagueDTO leagueDto = null;
    if (gambleseason.getActualSeason() != null &&
        gambleseason.getActualSeason().getLeague() != null) {

      leagueDto = new LeagueDTO(gambleseason.getActualSeason().getLeague());
    }

    return GambleSeasonDetailDTO.builder()
        .pk(gambleseason.getPk())
        .title(gambleseason.getTitle())
        .startedAt(gambleseason.getStartedAt())
        .finishedAt(gambleseason.getFinishedAt())
        .operatingStatus(gambleseason.getOperatingStatus())
        .description(gambleseason.getDescription())
        .league(leagueDto)
        .gambleSeasonTeams(seasonTeamList)
        .gambleSeasonRanking(rankingList)
        .build();
  }

}