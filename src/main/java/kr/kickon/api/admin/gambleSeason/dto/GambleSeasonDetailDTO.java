package kr.kickon.api.admin.gambleSeason.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "승부 예측 시즌 상세 응답 DTO")
public class GambleSeasonDetailDTO {

  @Schema(description = "승부 예측 시즌 PK", example = "1")
  private Long pk;

  @Schema(description = "리그")
  private LeagueDTO league;

  @Schema(description = "제목", example = "24/25 K 리그")
  private String title;

  @Schema(description = "시작일", example = "2025-03-16T12:00:00")
  private LocalDateTime startedAt;

  @Schema(description = "마감일", example = "2025-06-16T12:00:00")
  private LocalDateTime finishedAt;

  @Schema(description = "상태", example = "PROCEEDING")
  private OperatingStatus operatingStatus;

  @Schema(description = "설명(비고)")
  private String description;

  @Schema(description = "참여 팀 리스트")
  private List<SeasonTeamDTO> gambleSeasonTeams;

  @Schema(description = "팀 순위")
  private List<GetGambleSeasonRankingDTO> gambleSeasonRanking;

  public static GambleSeasonDetailDTO fromEntity(
      GambleSeason season,
      List<SeasonTeamDTO> seasonTeamList,
      List<GetGambleSeasonRankingDTO> rankingList
  ) {
    LeagueDTO leagueDto = null;
    if (season.getActualSeason() != null &&
        season.getActualSeason().getLeague() != null) {

      leagueDto = new LeagueDTO(season.getActualSeason().getLeague());
    }

    return GambleSeasonDetailDTO.builder()
        .pk(season.getPk())
        .title(season.getTitle())
        .startedAt(season.getStartedAt())
        .finishedAt(season.getFinishedAt())
        .operatingStatus(season.getOperatingStatus())
        .description(season.getDescription())
        .league(leagueDto)
        .gambleSeasonTeams(seasonTeamList)
        .gambleSeasonRanking(rankingList)
        .build();
  }

}