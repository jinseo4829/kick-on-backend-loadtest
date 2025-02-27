package kr.kickon.api.domain.migration.dto;

import lombok.Data;

@Data
public class ApiLeagueAndSeasonDTO {
    private ApiLeagueDTO league;
    private ApiSeasonDTO season;
    public ApiLeagueAndSeasonDTO(ApiLeagueDTO league, ApiSeasonDTO season) {
        this.league = league;
        this.season = season;
    }
}
