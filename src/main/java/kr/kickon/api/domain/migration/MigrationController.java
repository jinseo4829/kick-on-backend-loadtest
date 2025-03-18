package kr.kickon.api.domain.migration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.country.CountryService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.migration.dto.ApiGamesDTO;
import kr.kickon.api.domain.migration.dto.ApiLeagueAndSeasonDTO;
import kr.kickon.api.domain.migration.dto.ApiRankingDTO;
import kr.kickon.api.domain.migration.dto.ApiTeamDTO;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Country;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.slack.SlackService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/migration")
@Tag(name = "마이그레이션 관련")
@Slf4j
public class MigrationController {
    private final TeamService teamService;
    private final MigrationService migrationService;
    private final CountryService countryService;
    private final LeagueService leagueService;
    private final SlackService slackService;
    private final ActualSeasonService actualSeasonService;

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

    @Operation(summary = "리그 경기 불러오기", description = "각 리그의 경기를 불러오며, 상태값 및 경기 결과를 자동 업데이트")
    @PostMapping("/games")
    public ResponseEntity<ResponseDTO<Void>> fetchGames(@RequestParam String league, @RequestParam String season) {
//        List<League> leagues = leagueService.findAll();
        List<League> leagues = new ArrayList<>();
        League leagueByPk = leagueService.findByPk(Long.parseLong(league));
        if(leagueByPk == null) throw new NotFoundException(ResponseCode.NOT_FOUND_LEAGUE);
        leagues.add(leagueByPk);
        List<ApiGamesDTO> gamesFromApi = migrationService.fetchGames(leagues, season);
        migrationService.saveGames(gamesFromApi);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED));
    }

    @Operation(summary = "랭킹 불러오기", description = "각 리그의 랭킹을 불러오며, 하루하루 업데이트")
    @PostMapping("/rankings")
    @Scheduled(cron = "0 10 * * * *")
    public ResponseEntity<ResponseDTO<Void>> fetchRanking() {
        log.info("Scheduling: 랭킹 불러오기 => " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd / HH:mm:ss")));
        List<League> leagues = leagueService.findAllLeagues();
//        List<League> leagues = new ArrayList<>();
//        leagues.add(leagueService.findByPk(Long.parseLong(league)));

        List<ApiRankingDTO> rankingsFromApi = migrationService.fetchRankings(leagues);
        migrationService.saveRankings(rankingsFromApi);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED));
    }
}
