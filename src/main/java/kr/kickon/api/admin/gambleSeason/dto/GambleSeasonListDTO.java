package kr.kickon.api.admin.gambleSeason.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@Schema(description = "승부 예측 시즌 리스트 응답 DTO")
public class GambleSeasonListDTO {

  @Schema(description = "승부 예측 시즌 PK", example = "1")
  private Long pk;

  @Schema(description = "리그")
  private LeagueDTO league;

  @Schema(description = "제목", example = ExampleConstants.seasonTitle)
  private String title;

  @Schema(description = "시작일", example = ExampleConstants.startDatetime)
  private LocalDateTime startedAt;

  @Schema(description = "마감일", example = ExampleConstants.endDatetime)
  private LocalDateTime finishedAt;

  @Schema(description = "상태", example = ExampleConstants.operatingStatus)
  private OperatingStatus operatingStatus;

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

  public static GambleSeasonListDTO fromActualSeason(ActualSeason actualSeason) {
    LeagueDTO leagueDto = null;
    if (actualSeason.getLeague() != null) {
      leagueDto = new LeagueDTO(actualSeason.getLeague());
    }

    return GambleSeasonListDTO.builder()
        .pk(actualSeason.getPk())
        .title(actualSeason.getTitle())
        .startedAt(actualSeason.getStartedAt().atStartOfDay())
        .finishedAt(actualSeason.getFinishedAt().atStartOfDay())
        .operatingStatus(actualSeason.getOperatingStatus())
        .league(leagueDto)
        .build();
  }

}