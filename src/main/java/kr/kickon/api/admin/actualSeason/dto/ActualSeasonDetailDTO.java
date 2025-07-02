package kr.kickon.api.admin.actualSeason.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "실제 시즌 상세 응답 DTO")
public class ActualSeasonDetailDTO {

  @Schema(description = "시즌 PK", example = "1")
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

  @Schema(description = "설명(비고)")
  private String description;

  @Schema(description = "연도")
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
