package kr.kickon.api.domain.migration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.country.CountryService;
import kr.kickon.api.domain.game.GameService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.migration.dto.ApiGamesDTO;
import kr.kickon.api.domain.migration.dto.ApiLeagueAndSeasonDTO;
import kr.kickon.api.domain.migration.dto.ApiRankingDTO;
import kr.kickon.api.domain.migration.dto.ApiTeamDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Country;
import kr.kickon.api.global.common.entities.Game;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.kafka.KafkaGameProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@RestController
@RequestMapping("/migration")
@Tag(name = "마이그레이션 관련")
@Slf4j
public class MigrationController {
    private final MigrationService migrationService;
    private final CountryService countryService;
    private final LeagueService leagueService;
    private final GameService gameService;
    private final KafkaGameProducer kafkaGameProducer;

    @Operation(summary = "팀 불러오기", description = "각 리그 별로 속한 팀 불러오기")
    @PostMapping("/teams")
    public ResponseEntity<ResponseDTO<Void>> fetchTeams(@RequestParam String season) {
        List<League> leagues = leagueService.findAllBySeason(Integer.parseInt(season));
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

    @PostMapping("/test/{testId}")
    public void fetchTest(@RequestParam String body, @PathVariable String testId){
//        throw new NullPointerException();
        throw new NotFoundException(ResponseCode.NOT_FOUND_LEAGUE);
    }

//    @PostMapping("/test")
//    public ResponseEntity<ResponseDTO<Void>> fetchTest(){
//        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED));
//    }

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
    @Scheduled(cron = "0 */5 * * * *")
    public void fetchRanking() {
        List<League> leagues = leagueService.findAllLeagues();

        List<ApiRankingDTO> rankingsFromApi = migrationService.fetchRankings(leagues);
        migrationService.saveRankings(rankingsFromApi);
    }

    @Operation(summary = "게임 결과 불러오기",description = "게임결과 API 불러와서, 승부예측 마감 진행. 포인트 지급. 매일 오전 0시에 업데이트")
    @GetMapping("/gambles")
    @Scheduled(cron = "0 0 */3 * * *")
    public void fetchGambles() {
        List<Game> games = gameService.findByToday();

        List<ApiGamesDTO> apiGamesDTOS = migrationService.fetchGamesByApiIds(games);

        // 👇 여기 추가
        List<CompletableFuture<SendResult<String, ApiGamesDTO>>> futures = new ArrayList<>();
        for (ApiGamesDTO apiGame : apiGamesDTOS) {
            CompletableFuture<SendResult<String, ApiGamesDTO>> future =
                    kafkaGameProducer.sendGameResultProcessing(apiGame.getId().toString(), apiGame);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        migrationService.updateFinalTeamRanking();
    }
}
