package kr.kickon.api.admin.migration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.country.CountryService;
import kr.kickon.api.domain.gambleSeasonRanking.GambleSeasonRankingService;
import kr.kickon.api.domain.game.GameService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.admin.migration.dto.ApiGamesDTO;
import kr.kickon.api.admin.migration.dto.ApiLeagueAndSeasonDTO;
import kr.kickon.api.admin.migration.dto.ApiRankingDTO;
import kr.kickon.api.admin.migration.dto.ApiTeamDTO;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.*;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.kafka.KafkaGameProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@RestController
@RequestMapping("/admin/migration")
@Tag(name = "마이그레이션 관련")
@Slf4j
public class MigrationController {
    private final MigrationService migrationService;
    private final CountryService countryService;
    private final LeagueService leagueService;
    private final GameService gameService;
    private final KafkaGameProducer kafkaGameProducer;
    private final TeamService teamService;
    private final UserService userService;
    private final UserFavoriteTeamService userFavoriteTeamService;
    private final GambleSeasonRankingService gambleSeasonRankingService;
    private final ActualSeasonService actualSeasonService;

    @PostMapping("/ai-user")
    @Operation(summary = "AI 유저 마이그레이션", description = "팀당 하나씩 AI 유저를 생성합니다. 이미 존재하는 경우 건너뜁니다.")
    public ResponseEntity<ResponseDTO<String>> migrateAiUsers() {
        List<Team> teams = teamService.findAll();

        int createdCount = 0;
        for (Team team : teams) {
            String baseName = team.getNameKr() != null && !team.getNameKr().isBlank()
                    ? team.getNameKr()
                    : team.getNameEn(); // fallback
            String aiEmail = baseName.replaceAll("\\s+", "").toLowerCase() + "@kick-on.ai"; // ✅ 도메인 변경

            // 이미 AI 유저가 있는지 확인
            if (userService.existsByEmailAndProvider(aiEmail, ProviderType.AI)) {
                log.info("✅ 이미 존재: {}", aiEmail);
                continue;
            }

            // AI 유저 생성
            User aiUser = User.builder()
                    .id(UUID.randomUUID().toString())
                    .email(aiEmail)
                    .provider(ProviderType.AI)
                    .providerId("AI")
                    .userStatus(UserAccountStatus.DEFAULT)
                    .nickname(team.getNameKr()!=null ? team.getNameKr() : team.getNameEn() + "AI")
                    .privacyAgreedAt(LocalDateTime.now())
                    .build();

            userService.saveUser(aiUser);
            createdCount++;

            // AI 유저의 응원팀 설정
            UserFavoriteTeam favoriteTeam = UserFavoriteTeam.builder()
                    .user(aiUser)
                    .team(team)
                    .priorityNum(1)
                    .build();
            userFavoriteTeamService.save(favoriteTeam);
        }

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, createdCount + "명 생성 완료"));
    }

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
        System.out.println(Arrays.toString(leaguesAndSeasons.toArray()));
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
        List<Game> games = gameService.getPendingGames();
        List<ApiGamesDTO> apiGamesDTOS = migrationService.fetchGamesByApiIds(games);
        // 👇 여기 추가
        List<CompletableFuture<SendResult<String, ApiGamesDTO>>> futures = new ArrayList<>();

        for (ApiGamesDTO apiGame : apiGamesDTOS) {
            CompletableFuture<SendResult<String, ApiGamesDTO>> future =
                    kafkaGameProducer.sendGameResultProcessing(apiGame.getId().toString(), apiGame);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();


        List<League> leagues = leagueService.findAllLeagues();
        for(League league : leagues) {
            ActualSeason actualSeason = actualSeasonService.findRecentByLeaguePk(league.getPk());
            gambleSeasonRankingService.updateGameNumOnlyByActualSeason(actualSeason.getPk());
        }

        migrationService.updateFinalTeamRanking();
    }
}