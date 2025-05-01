package kr.kickon.api.domain.migration.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.kickon.api.global.common.entities.ActualSeason;
import lombok.*;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiGamesDTO {
    private Long id;
    private String round;
    private LocalDateTime date;
    private String status;
    private Integer homeScore;
    private Integer awayScore;
    private Long homeTeamId;
    private Long awayTeamId;
    private ActualSeason actualSeason;
    private Integer homePenaltyScore;
    private Integer awayPenaltyScore;

    @Override
    public String toString() {
        return "ApiGamesDTO [id=" + id + ", round=" + round + ", date=" + date+ ", status=" + status+ ", homeScore="+homeScore+", awayScore="+awayScore+"]";
    }
}
