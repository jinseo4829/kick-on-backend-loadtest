package kr.kickon.api.domain.migration;

import com.slack.api.methods.SlackApiException;
import io.swagger.v3.oas.annotations.Operation;
import kr.kickon.api.domain.country.CountryService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.migration.dto.ApiLeagueAndSeasonDTO;
import kr.kickon.api.domain.migration.dto.ApiTeamDTO;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Country;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.slack.SlackService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/migration")
public class MigrationController {
    private final TeamService teamService;
    private final MigrationService migrationService;
    private final CountryService countryService;
    private final LeagueService leagueService;
    private final SlackService slackService;

    @Operation(summary = "팀 불러오기", description = "각 리그 별로 속한 팀 불러오기")
    @PostMapping("/teams")
    public ResponseEntity<ResponseDTO<Void>> fetchTeams(@RequestParam String season) {
        List<League> leagues = leagueService.findAll();
        List<ApiTeamDTO> teams = migrationService.fetchTeams(leagues,Integer.parseInt(season));
        migrationService.saveTeamsAndSeasonTeams(teams);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED));
    }

    @Operation(summary = "리그 및 시즌 불러오기", description = "각 리그와 시즌 불러오기")
    @PostMapping("/leagues")
    public ResponseEntity<ResponseDTO<Void>> fetchLeaguesAndSeasons(@RequestParam String season) {
        List<Country> countries = countryService.findAll();
        List<ApiLeagueAndSeasonDTO> leaguesAndSeasons = migrationService.fetchLeaguesAndSeasons(countries,Integer.parseInt(season));
        migrationService.saveLeagueAndSeason(leaguesAndSeasons);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED));
    }

    @PostMapping("/test")
    public void fetchTest(){
        throw new NotFoundException(ResponseCode.NOT_FOUND_USER);
    }
}
