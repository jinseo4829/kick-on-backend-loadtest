package kr.kickon.api.admin.gambleSeason.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "승부 예측 시즌 리스트 응답 DTO")
public class GambleSeasonListDTO {

  @Schema(description = "승부 예측 시즌 PK", example = "1")
  private Long pk;

  @Schema(description = "리그", example = "PENDING")
  private LeagueDTO league;

  @Schema(description = "제목", example = "24/25 K 리그")
  private String title;

  @Schema(description = "시작일", example = "2025-03-16T12:00:00")
  private LocalDateTime startedAt;

  @Schema(description = "마감일", example = "2025-06-16T12:00:00")
  private LocalDateTime finishedAt;

  @Schema(description = "상태", example = "PROCEEDING")
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