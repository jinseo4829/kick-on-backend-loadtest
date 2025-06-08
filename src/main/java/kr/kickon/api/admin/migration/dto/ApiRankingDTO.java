package kr.kickon.api.admin.migration.dto;

import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.Team;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiRankingDTO {
    Integer rankOrder;
    Integer gameNum;
    Integer wins;
    Integer draws;
    Integer loses;
    Integer wonScores;
    Integer lostScores;
    Integer points;
    ActualSeason actualSeason;
    Team team;
    Integer season;
}
