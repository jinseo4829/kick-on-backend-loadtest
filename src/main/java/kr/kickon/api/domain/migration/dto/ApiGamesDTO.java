package kr.kickon.api.domain.migration.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiGamesDTO {
    private Long id;
    private String round;
    private LocalDateTime date;
    private String status;
    private Integer homeScore;
    private Integer awayScore;
    private Long homeTeamId;
    private Long awayTeamId;
    private Long actualSeasonPk;

    @JsonCreator
    public ApiGamesDTO(
            @JsonProperty("id") String id,
            @JsonProperty("round") String round,
            @JsonProperty("date") String date
    ) {
        this.id = Long.valueOf(id);
        this.round = round;
        this.date = LocalDateTime.parse(date);
    }
}
