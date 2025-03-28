package kr.kickon.api.domain.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.actualSeasonRanking.ActualSeasonRankingService;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.gambleSeason.GambleSeasonService;
import kr.kickon.api.domain.gambleSeasonPoint.GambleSeasonPointService;
import kr.kickon.api.domain.gambleSeasonRanking.GambleSeasonRankingService;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.domain.game.GameService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.migration.dto.*;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.userGameGamble.UserGameGambleService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.GambleStatus;
import kr.kickon.api.global.common.enums.GameStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MigrationService {
    private final WebClient webClient;
    private final ActualSeasonRankingService actualSeasonRankingService;
    private final LeagueService leagueService;
    private final TeamService teamService;
    private final ActualSeasonService actualSeasonService;
    private final UUIDGenerator uuidGenerator;
    private final ActualSeasonTeamService actualSeasonTeamService;
    private final GameService gameService;
    private final UserGameGambleService userGameGambleService;
    private final GambleSeasonPointService gambleSeasonPointService;
    private final GambleSeasonRankingService gambleSeasonRankingService;
    private final GambleSeasonService gambleSeasonService;

    public MigrationService(@Value("${api.key}") String apiKey, LeagueService leagueService, TeamService teamService, ActualSeasonService actualSeasonService, UUIDGenerator uuidGenerator, ActualSeasonTeamService actualSeasonTeamService, GameService gameService, ActualSeasonRankingService actualSeasonRankingService, UserGameGambleService userGameGambleService, GambleSeasonPointService gambleSeasonPointService, GambleSeasonRankingService gambleSeasonRankingService, GambleSeasonService gambleSeasonService) {
        this.gambleSeasonService = gambleSeasonService;
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)) // to unlimited memory size
                .build();
        this.webClient = WebClient.builder()
                .baseUrl("https://v3.football.api-sports.io")
                .defaultHeader("x-rapidapi-host","v3.football.api-sports.io")
                .defaultHeader("x-rapidapi-key", apiKey)
                .exchangeStrategies(exchangeStrategies)
                .build();
        this.leagueService = leagueService;
        this.teamService = teamService;
        this.actualSeasonService = actualSeasonService;
        this.uuidGenerator = uuidGenerator;
        this.actualSeasonTeamService = actualSeasonTeamService;
        this.gameService = gameService;
        this.actualSeasonRankingService = actualSeasonRankingService;
        this.userGameGambleService = userGameGambleService;
        this.gambleSeasonPointService = gambleSeasonPointService;
        this.gambleSeasonRankingService = gambleSeasonRankingService;
    }

    @Transactional
    public void saveRankings(List<ApiRankingDTO> list) {
        // 랭킹 아이디 체크
        List<String> rankingIds = new ArrayList<>();
        list.forEach(apiData -> {
            ActualSeasonRanking existActualSeasonRanking = actualSeasonRankingService.findByActualSeasonAndTeam(apiData.getActualSeason().getPk(),apiData.getTeam().getPk());
            ActualSeasonRanking actualSeasonRanking;
            if(existActualSeasonRanking == null) {
                String rankingId = "";
                do {
                    try{
                        rankingId = uuidGenerator.generateUniqueUUID(leagueService::findById);
                    }catch (NotFoundException ignore2){
                    }
                    // 이미 생성된 ID가 배열에 있는지 확인
                    if(!rankingIds.contains(rankingId)) {
                        break;
                    }
                } while (true); // 중복이 있을 경우 다시 생성
                rankingIds.add(rankingId);
                actualSeasonRanking = ActualSeasonRanking.builder()
                        .id(rankingId)
                        .actualSeason(apiData.getActualSeason())
                        .season(apiData.getSeason())
                        .loses(apiData.getLoses())
                        .wins(apiData.getWins())
                        .draws(apiData.getDraws())
                        .gameNum(apiData.getGameNum())
                        .lostScores(apiData.getLostScores())
                        .wonScores(apiData.getWonScores())
                        .rankOrder(apiData.getRankOrder())
                        .team(apiData.getTeam())
                        .points(apiData.getPoints())
                        .build();
            }else {
                actualSeasonRanking = existActualSeasonRanking;
                actualSeasonRanking.setLoses(apiData.getLoses());
                actualSeasonRanking.setWins(apiData.getWins());
                actualSeasonRanking.setDraws(apiData.getDraws());
                actualSeasonRanking.setGameNum(apiData.getGameNum());
                actualSeasonRanking.setLostScores(apiData.getLostScores());
                actualSeasonRanking.setWonScores(apiData.getWonScores());
                actualSeasonRanking.setRankOrder(apiData.getRankOrder());
                actualSeasonRanking.setPoints(apiData.getPoints());
            }

            actualSeasonRankingService.save(actualSeasonRanking);
        });
    }

    @Transactional
    public void saveGames(List<ApiGamesDTO> list){
        // 게임 아이디 체크
        List<String> gameIds = new ArrayList<>();
        List<String> scheduledStatus = new ArrayList<>(Arrays.asList(GameService.ScheduledStatus));
        List<String> finishedStatus = new ArrayList<>(Arrays.asList(GameService.FinishedStatus));
        
        list.forEach(apiData -> {
            Game game = null;
            GameStatus gameStatus = getGameStatus(apiData, scheduledStatus, finishedStatus);
            try {
                // 필수 값 체크
                game = gameService.findByApiId(apiData.getId());
                game.setGameStatus(gameStatus);
                game.setAwayPenaltyScore(apiData.getAwayPenaltyScore());
                game.setHomePenaltyScore(apiData.getHomePenaltyScore());
                game.setHomeScore(apiData.getHomeScore());
                game.setAwayScore(apiData.getAwayScore());
            } catch (NotFoundException ignore) {
                Optional<Team> homeTeam, awayTeam;
                homeTeam = Optional.ofNullable(teamService.findByApiId(apiData.getHomeTeamId()));
                awayTeam = Optional.ofNullable(teamService.findByApiId(apiData.getAwayTeamId()));
                if (homeTeam.isEmpty() || awayTeam.isEmpty()) {
                } else {
                    String gameId = "";
                    do {
                        try {
                            gameId = uuidGenerator.generateUniqueUUID(leagueService::findById);
                        } catch (NotFoundException ignore2) {
                        }
                        // 이미 생성된 ID가 배열에 있는지 확인
                        if (!gameIds.contains(gameId)) {
                            break;
                        }
                    } while (true); // 중복이 있을 경우 다시 생성
                    gameIds.add(gameId);

                    game = Game.builder()
                            .id(gameId)
                            .gameStatus(gameStatus)
                            .awayPenaltyScore(apiData.getAwayPenaltyScore())
                            .homePenaltyScore(apiData.getHomePenaltyScore())
                            .actualSeason(apiData.getActualSeason())
                            .apiId(apiData.getId())
                            .homeScore(apiData.getHomeScore())
                            .awayScore(apiData.getAwayScore())
                            .round(apiData.getRound())
                            .homeTeam(homeTeam.get())
                            .awayTeam(awayTeam.get())
                            .startedAt(apiData.getDate())
                            .build();
                }

            }
            gameService.save(game);
        });
    }

    public void saveGamesAndUpdateGambles(List<ApiGamesDTO> list){
        // 게임 아이디 체크
        List<String> gameIds = new ArrayList<>();
        List<String> scheduledStatus = new ArrayList<>(Arrays.asList(GameService.ScheduledStatus));
        List<String> finishedStatus = new ArrayList<>(Arrays.asList(GameService.FinishedStatus));
        List<String> homeGambleSeasonPointIds = new ArrayList<>();
        List<String> awayGambleSeasonPointIds = new ArrayList<>();
        List<String> homeGambleSeasonPoint = new ArrayList<>();
        list.forEach(apiData -> {
            Game game = null;
            GameStatus gameStatus = getGameStatus(apiData, scheduledStatus, finishedStatus);
            try{
                // 필수 값 체크
                game = gameService.findByApiId(apiData.getId());
                game.setGameStatus(gameStatus);
                game.setAwayPenaltyScore(apiData.getAwayPenaltyScore());
                game.setHomePenaltyScore(apiData.getHomePenaltyScore());
                game.setHomeScore(apiData.getHomeScore());
                game.setAwayScore(apiData.getAwayScore());
                List<UserGameGamble> userGameGambles = userGameGambleService.findByGameApiId(game.getApiId());
                userGameGambleService.updateGambleStatusByApiGamesDTO(userGameGambles, apiData, gameStatus);
            }catch (NotFoundException ignore){
                Optional<Team> homeTeam, awayTeam;
                homeTeam = Optional.ofNullable(teamService.findByApiId(apiData.getHomeTeamId()));
                awayTeam = Optional.ofNullable(teamService.findByApiId(apiData.getAwayTeamId()));
                if(homeTeam.isEmpty() || awayTeam.isEmpty()){}
                else{
                    String gameId = "";
                    do {
                        try{
                            gameId = uuidGenerator.generateUniqueUUID(leagueService::findById);
                        }catch (NotFoundException ignore2){
                        }
                        // 이미 생성된 ID가 배열에 있는지 확인
                        if(!gameIds.contains(gameId)) {
                            break;
                        }
                    } while (true); // 중복이 있을 경우 다시 생성
                    gameIds.add(gameId);

                    game = Game.builder()
                            .id(gameId)
                            .gameStatus(gameStatus)
                            .awayPenaltyScore(apiData.getAwayPenaltyScore())
                            .homePenaltyScore(apiData.getHomePenaltyScore())
                            .actualSeason(apiData.getActualSeason())
                            .apiId(apiData.getId())
                            .homeScore(apiData.getHomeScore())
                            .awayScore(apiData.getAwayScore())
                            .round(apiData.getRound())
                            .homeTeam(homeTeam.get())
                            .awayTeam(awayTeam.get())
                            .startedAt(apiData.getDate())
                            .build();
                }

            }
            game = gameService.save(game);
            List<UserGameGamble> userGameGambles = userGameGambleService.findByGameApiId(game.getApiId());
            // 홈팀과 어웨이팀으로 구분
            List<UserGameGamble> homeTeamGambles = userGameGambles.stream()
                    .filter(g -> g.getSupportingTeam().getApiId().equals(apiData.getHomeTeamId()))
                    .toList();

            List<UserGameGamble> awayTeamGambles = userGameGambles.stream()
                    .filter(g -> g.getSupportingTeam().getApiId().equals(apiData.getAwayTeamId()))
                    .toList();

            String homeGambleSeasonPointId = "";
            String awayGambleSeasonPointId = "";
            do {
                homeGambleSeasonPointId = uuidGenerator.generateUniqueUUID(gambleSeasonPointService::findById);
                // 이미 생성된 ID가 배열에 있는지 확인
            } while (homeGambleSeasonPointIds.contains(homeGambleSeasonPointId)); // 중복이 있을 경우 다시 생성
            do {
                awayGambleSeasonPointId = uuidGenerator.generateUniqueUUID(gambleSeasonPointService::findById);
                // 이미 생성된 ID가 배열에 있는지 확인
            } while (awayGambleSeasonPointIds.contains(awayGambleSeasonPointId)); // 중복이 있을 경우 다시 생성
            homeGambleSeasonPointIds.add(homeGambleSeasonPointId);
            awayGambleSeasonPointIds.add(awayGambleSeasonPointId);

            // 홈팀 평균 포인트 저장
            double homeAvgPoints = calculateAveragePoints(homeTeamGambles);
            GambleSeasonPoint homeSeasonPoint = GambleSeasonPoint.builder()
                    .id(homeGambleSeasonPointId)
                    .averagePoints((int) Math.round(homeAvgPoints * 1000))
                    .team(teamService.findByApiId(apiData.getHomeTeamId()))
                    .game(game)
                    .build();
            gambleSeasonPointService.save(homeSeasonPoint);

            // 어웨이팀 평균 포인트 저장
            double awayAvgPoints = calculateAveragePoints(awayTeamGambles);
            GambleSeasonPoint awaySeasonPoint = GambleSeasonPoint.builder()
                    .id(awayGambleSeasonPointId)
                    .averagePoints((int) Math.round(awayAvgPoints * 1000))
                    .team(teamService.findByApiId(apiData.getAwayTeamId()))
                    .game(game)
                    .build();
            gambleSeasonPointService.save(awaySeasonPoint);
            // 랭킹 업데이트
            GambleSeasonRanking homeGambleSeasonRanking = gambleSeasonRankingService.findByTeamPk(homeSeasonPoint.getTeam().getPk());
            homeGambleSeasonRanking.setPoints(homeGambleSeasonRanking.getPoints() + homeSeasonPoint.getAveragePoints());
            homeGambleSeasonRanking.setGameNum(homeGambleSeasonRanking.getGameNum()+1);
            GambleSeasonRanking awayGambleSeasonRanking = gambleSeasonRankingService.findByTeamPk(awaySeasonPoint.getTeam().getPk());
            awayGambleSeasonRanking.setPoints(awayGambleSeasonRanking.getPoints() + awaySeasonPoint.getAveragePoints());
            homeGambleSeasonRanking.setGameNum(awayGambleSeasonRanking.getGameNum()+1);
            gambleSeasonRankingService.save(homeGambleSeasonRanking);
            gambleSeasonRankingService.save(awayGambleSeasonRanking);
        });
        updateTeamRankings();
    }

    // 평균 포인트 계산 메서드
    double calculateAveragePoints(List<UserGameGamble> gambles) {
        return gambles.stream()
                .filter(g -> g.getGambleStatus() == GambleStatus.SUCCEED || g.getGambleStatus() == GambleStatus.PERFECT)
                .mapToInt(g -> g.getGambleStatus() == GambleStatus.PERFECT ? 3 : 1)
                .average()
                .orElse(0.0);
    }

    private void updateTeamRankings() {
        // 팀 랭킹을 업데이트하고 rank_order를 새로 계산
        List<League> leagues = leagueService.findAll();
        for(League league : leagues) {
            GambleSeason gambleSeason;
            try {
                gambleSeason = gambleSeasonService.findRecentOperatingSeasonByLeaguePk(league.getPk());
            }catch (Exception e) {
                continue;
            }

            List<GambleSeasonRanking> rankings = gambleSeasonRankingService.findRecentSeasonRankingByGambleSeason(gambleSeason.getPk());
            if(rankings == null) continue;
            gambleSeasonRankingService.recalculateRanking(rankings);
        }
    }

    private static @NotNull GameStatus getGameStatus(ApiGamesDTO apiData, List<String> scheduledStatus, List<String> finishedStatus) {
        GameStatus gameStatus;
        if(scheduledStatus.contains(apiData.getStatus())){
            // 시작 안한 경기
            gameStatus = GameStatus.PENDING;
        }

        else if(finishedStatus.contains(apiData.getStatus())){
            // 끝난 경기
            if(apiData.getStatus().equals("PEN")){
                // 승부차기인지 체크
                gameStatus = apiData.getHomePenaltyScore() > apiData.getAwayPenaltyScore() ? GameStatus.HOME : GameStatus.AWAY;
            } else {
                // 일반적으로 경기 마무리 된 경우
                gameStatus = apiData.getHomeScore().equals(apiData.getAwayScore()) ? GameStatus.DRAW : apiData.getHomeScore() > apiData.getAwayScore() ? GameStatus.HOME : GameStatus.AWAY;
            }
        } else if(apiData.getStatus().equals("PST")){
            // 연기된 경기
            gameStatus = GameStatus.POSTPONED;
        } else if(apiData.getStatus().equals("CANC") || apiData.getStatus().equals("ABD")){
            // 취소된 경기
            gameStatus = GameStatus.CANCELED;
        } else {
            // 진행중인 경기
            gameStatus = GameStatus.PROCEEDING;
        }
        return gameStatus;
    }


    @Transactional
    public void saveLeagueAndSeason(List<ApiLeagueAndSeasonDTO> list){
        List<String> leagueIds = new ArrayList<>();
        List<String> seasonIds = new ArrayList<>();
        list.forEach(apiData -> {
            ApiLeagueDTO apiLeague = apiData.getLeague();
            ApiSeasonDTO apiSeason = apiData.getSeason();
            String leagueId="", actualSeasonId = "";
            League league;
            ActualSeason actualSeason;
            try {
                league = leagueService.findByApiId(apiLeague.getId());
                league.setEnName(apiLeague.getName());
                league.setType(apiLeague.getType());
                league.setLogoUrl(apiLeague.getLogo());
                leagueService.save(league);
            }catch (NotFoundException e) {
                // 중복되지 않는 ID를 생성할 때까지 반복
                do {
                    try{
                        leagueId = uuidGenerator.generateUniqueUUID(leagueService::findById);
                    }catch (NotFoundException ignore){
                    }
                    // 이미 생성된 ID가 배열에 있는지 확인
                    if(!leagueIds.contains(leagueId)) {
                        break;
                    }
                } while (true); // 중복이 있을 경우 다시 생성
                leagueIds.add(leagueId);
                league = League.builder()
                        .id(leagueId)
                        .apiId(apiLeague.getId())
                        .type(apiLeague.getType())
                        .logoUrl(apiLeague.getLogo())
                        .build();
                leagueService.save(league);
            }
            actualSeason = actualSeasonService.findByYearAndLeague(apiSeason.getYear(),league.getPk());
            if(actualSeason == null) {
                // 중복되지 않는 ID를 생성할 때까지 반복
                do {
                    try{
                        actualSeasonId = uuidGenerator.generateUniqueUUID(actualSeasonService::findById);
                    }catch (NotFoundException ignore){
                    }
                    // 이미 생성된 ID가 배열에 있는지 확인
                    if(!seasonIds.contains(actualSeasonId)) {
                        break;
                    }
                } while (true); // 중복이 있을 경우 다시 생성
                seasonIds.add(actualSeasonId);
                actualSeason = ActualSeason.builder()
                        .id(actualSeasonId)
                        .operatingStatus(apiSeason.getOperatingStatus())
                        .year(apiSeason.getYear())
                        .league(league)
                        .startedAt(apiSeason.getStart())
                        .finishedAt(apiSeason.getEnd())
                        .build();
                actualSeasonService.save(actualSeason);
            }else{
                actualSeason.setYear(apiSeason.getYear());
                actualSeason.setStartedAt(apiSeason.getStart());
                actualSeason.setFinishedAt(apiSeason.getEnd());
                actualSeason.setFinishedAt(apiSeason.getEnd());
                apiSeason.setOperatingStatus(apiSeason.getOperatingStatus());
                actualSeasonService.save(actualSeason);
            }
        });
    }

    @Transactional
    public void saveTeamsAndSeasonTeams(List<ApiTeamDTO> apiTeams){
        List<String> ids = new ArrayList<>();
        List<String> actualSeasonTeamIds = new ArrayList<>();

        apiTeams.forEach(apiTeam -> {
            String id="";

            Team team = teamService.findByApiId(apiTeam.getId());
            Team teamObj;

            if(team != null) {
                teamObj = team;
                teamObj.setCode(apiTeam.getCode());
                teamObj.setNameEn(apiTeam.getName());
                teamObj.setLogoUrl(apiTeam.getLogo());
                teamObj = teamService.save(teamObj);
            }else{
                // 중복되지 않는 ID를 생성할 때까지 반복
                do {
                    try{
                        id = uuidGenerator.generateUniqueUUID(teamService::findById);
                    }catch (NotFoundException ignore){
                    }
                    // 이미 생성된 ID가 배열에 있는지 확인
                    if(!ids.contains(id)) {
                        break;
                    }
                } while (true); // 중복이 있을 경우 다시 생성
                ids.add(id);
                teamObj = Team.builder()
                        .id(id)
                        .nameEn(apiTeam.getName())
                        .code(apiTeam.getCode())
                        .logoUrl(apiTeam.getLogo())
                        .apiId(apiTeam.getId())
                        .build();
                teamObj = teamService.save(teamObj);
            }
            ActualSeason actualSeason = actualSeasonService.findByYearAndLeague(apiTeam.getYear(),apiTeam.getLeaguePk());
            if(actualSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
            try {
                actualSeasonTeamService.findByActualSeasonTeam(actualSeason,teamObj.getPk());
            }catch (NotFoundException e){
                String actualSeasonTeamId="";
                do {
                    try{
                        actualSeasonTeamId = uuidGenerator.generateUniqueUUID(actualSeasonTeamService::findById);
                    }catch (NotFoundException ignore){
                    }
                    // 이미 생성된 ID가 배열에 있는지 확인
                    if(!actualSeasonTeamIds.contains(actualSeasonTeamId)) {
                        break;
                    }
                } while (true); // 중복이 있을 경우 다시 생성
                actualSeasonTeamIds.add(actualSeasonTeamId);
                ActualSeasonTeam actualSeasonObj = ActualSeasonTeam.builder()
                        .id(actualSeasonTeamId)
                        .team(teamObj)
                        .actualSeason(actualSeason)
                        .build();
                actualSeasonTeamService.save(actualSeasonObj);
            }
        });
    }

    public List<ApiRankingDTO> fetchRankings(List<League> leagues){
        List<ApiRankingDTO> list = new ArrayList<>();
        for(League league : leagues) {
            ActualSeason actualSeason;
            actualSeason = actualSeasonService.findRecentByLeaguePk(league.getPk());
            if(actualSeason == null) continue;
            Map<String, Object> response = webClient.get().uri(uriBuilder ->
                            uriBuilder.path("/standings")
                                    .queryParam("league",league.getApiId())
                                    .queryParam("season",actualSeason.getYear())
                                    .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            List<Map<String, Object>> responseData = (List<Map<String, Object>>) response.get("response");
            // "fixture" 데이터 추출

            if(responseData.isEmpty()) continue;
            Map<String, Object> leagueData = (Map<String, Object>) responseData.get(0).get("league");
            List<List<Map<String, Object>>> rankingDatas = (List<List<Map<String, Object>>>) leagueData.get("standings");
            List<Map<String, Object>> flattenedList = rankingDatas.stream().flatMap(List::stream)
                            .collect(Collectors.toList());
            list.addAll(flattenedList.stream()
                    .filter(rankingData -> {
                        String group = (String) rankingData.get("group");
                        return group != null && group.contains("Regular Season");
                    })
                    .map(rankingData -> {
                        // DTO 객체 생성
                        Map<String, Object> teamData = (Map<String, Object>) rankingData.get("team");
                        Map<String, Object> metaData = (Map<String, Object>) rankingData.get("all");
                        Map<String, Object> goalData = (Map<String, Object>) metaData.get("goals");
//                        log.error(rankingData.toString());

                        Team team = teamService.findByApiId(Long.valueOf((Integer) teamData.get("id")));

                        if(team == null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
                        return ApiRankingDTO.builder()
                                .rankOrder((Integer) rankingData.get("rank"))
                                .team(team)
                                .actualSeason(actualSeason)
                                .draws((Integer) metaData.get("draw"))
                                .wins((Integer) metaData.get("win"))
                                .loses((Integer) metaData.get("lose"))
                                .gameNum((Integer) metaData.get("played"))
                                .lostScores((Integer) goalData.get("against"))
                                .wonScores((Integer) goalData.get("for"))
                                .points((Integer) rankingData.get("points"))
                                .season(actualSeason.getYear())
                                .build();
                    }).toList());
        }
        return list;
    }

    public List<ApiGamesDTO> fetchGames(List<League> leagues, String season){
        List<ApiGamesDTO> list = new ArrayList<>();
        for(League league : leagues) {
            ActualSeason actualSeason = actualSeasonService.findByYearAndLeague(Integer.parseInt(season),league.getPk());
            if(actualSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
            Map<String, Object> response = webClient.get().uri(uriBuilder ->
                    uriBuilder.path("/fixtures")
                            .queryParam("league",league.getApiId())
                            .queryParam("season",season)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) response.get("response");
            list.addAll(
                    responseList.stream()
                            .filter(responseData -> {
                                // "league" 데이터에서 round 가져오기
                                Map<String, Object> leagueData = (Map<String, Object>) responseData.get("league");
                                String round = (String) leagueData.get("round");
                                return round.contains("Regular Season"); // Regular Season만 필터링
                            })
                            .map(responseData -> {
                                // "fixture" 데이터 추출
                                Map<String, Object> fixtureData = (Map<String, Object>) responseData.get("fixture");
                                Long fixtureId = Long.valueOf((Integer) fixtureData.get("id"));
                                String date = (String) fixtureData.get("date");
                                Map<String, Object> statusData = (Map<String, Object>) fixtureData.get("status");
                                String status = (String) statusData.get("short");

                                // "league" 데이터 추출
                                Map<String, Object> leagueData = (Map<String, Object>) responseData.get("league");

                                // 날짜 변환 (ISO-8601 -> LocalDateTime)
                                LocalDateTime startedAt = LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);



                                // "teams" 데이터 추출
                                Map<String, Object> teamData = (Map<String, Object>) responseData.get("teams");
                                Map<String, Object> homeTeamData = (Map<String, Object>) teamData.get("home");
                                Map<String, Object> awayTeamData = (Map<String, Object>) teamData.get("away");
                                Long homeTeamId = Long.valueOf((Integer) homeTeamData.get("id"));
                                Long awayTeamId = Long.valueOf((Integer) awayTeamData.get("id"));

                                // "goals" 데이터 추출
                                Map<String, Object> goalsData = (Map<String, Object>) responseData.get("goals");
                                Integer homeScore = (Integer) goalsData.get("home");
                                Integer awayScore = (Integer) goalsData.get("away");

                                Integer homePenaltyScore = null,awayPenaltyScore = null;
                                if(status.equals("PEN")){
                                    Map<String, Object> scoreData = (Map<String, Object>) responseData.get("score");
                                    Map<String, Object> penaltyData = (Map<String, Object>) scoreData.get("penalty");
                                    homePenaltyScore = (Integer) penaltyData.get("home");
                                    awayPenaltyScore = (Integer) penaltyData.get("away");
                                }

                                // DTO 객체 생성
                                return ApiGamesDTO.builder()
                                        .id(fixtureId)
                                        .round((String) leagueData.get("round"))
                                        .actualSeason(actualSeason)
                                        .date(startedAt)
                                        .awayScore(awayScore)
                                        .homeScore(homeScore)
                                        .homeTeamId(homeTeamId)
                                        .awayTeamId(awayTeamId)
                                        .status(status)
                                        .homePenaltyScore(homePenaltyScore)
                                        .awayPenaltyScore(awayPenaltyScore)
                                        .build();
                            })
                            .collect(Collectors.toList())
            );
        }

        return list;
    }

    public List<ApiLeagueAndSeasonDTO> fetchLeaguesAndSeasons(List<Country> countries, Integer season){
        List<League> leagues = leagueService.findAll();
        List<Long> leagueIds = leagues.stream()
                .map(League::getApiId) // League 객체에서 api_id 추출
                .collect(Collectors.toList());
        List<ApiLeagueAndSeasonDTO> list = new ArrayList<>();
        for(Country country : countries){
            Map<String, Object> response = webClient.get().uri(uriBuilder ->
                            uriBuilder.path("/leagues")
                                    .queryParam("code",country.getCode())
                                    .queryParam("season",season)
                                    .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) response.get("response");
            list.addAll(responseList.stream()
                    .map(responseData -> {
                        Object leagueData = responseData.get("league");
                        List<Object> seasonData = (List<Object>) responseData.get("seasons");
                        ApiLeagueDTO apiLeagueDTO = null;
                        ApiSeasonDTO apiSeasonDTO = null;

                        // Map을 ApiLeagueDTO로 변환
                        if (leagueData instanceof Map) {
                            ObjectMapper objectMapper = new ObjectMapper();
                            apiLeagueDTO = objectMapper.convertValue(leagueData, ApiLeagueDTO.class);
                        }
                        if(apiLeagueDTO != null){
                            // Map을 ApiSeasonDTO로 변환
                            if (seasonData.get(0) instanceof Map) {
                                ObjectMapper objectMapper = new ObjectMapper();
                                apiSeasonDTO = objectMapper.convertValue(seasonData.get(0), ApiSeasonDTO.class);
                            }
                        }

                        // ApiLeagueAndSeasonDTO 객체 반환
                        return new ApiLeagueAndSeasonDTO(apiLeagueDTO, apiSeasonDTO);
                    })
                    .filter(responseData -> leagueIds.contains(responseData.getLeague().getId()))
                    .collect(Collectors.toList()));
        }
        return list;
    }

    public List<ApiTeamDTO> fetchTeams(List<League> leagues, Integer season){
        List<ApiTeamDTO> teams = new ArrayList<>();
        for(League league : leagues){
            Map<String, Object> response = webClient.get().uri(uriBuilder -> uriBuilder.path("/teams")
                            .queryParam("league", league.getApiId())
                            .queryParam("season", season)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) response.get("response");

            teams.addAll(responseList.stream()
                    .map(responseData -> {
                        Object data = responseData.get("team");
                        ApiTeamDTO apiTeamDTO = null;
                        // Map을 ApiTeamDTO로 변환
                        if (data instanceof Map) {
                            ObjectMapper objectMapper = new ObjectMapper();
                            apiTeamDTO = objectMapper.convertValue(data, ApiTeamDTO.class);
                            apiTeamDTO.setYear(season);
                            apiTeamDTO.setLeaguePk(league.getPk());
                        }

                        return apiTeamDTO;
                    })
                    .collect(Collectors.toList()));
        }

        return teams;
    }
    public List<ApiGamesDTO> fetchGamesByApiIds(List<Game> games){
        List<ApiGamesDTO> list = new ArrayList<>();
        // 게임 ID를 20개씩 나누어 처리
        for (int i = 0; i < games.size(); i += 20) {
            // 20개씩 나누어 게임 ID를 sublist로 가져옴
            List<Long> gameIds = games.subList(i, Math.min(i + 20, games.size()))
                    .stream()
                    .map(Game::getApiId) // Game 객체에서 apiId 가져오기
                    .toList();

            // API 호출
            Map<String, Object> response = webClient.get().uri(uriBuilder ->
                            uriBuilder.path("/fixtures")
                                    .queryParam("ids", String.join("-", gameIds.stream().map(String::valueOf).collect(Collectors.toList())))
                                    .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 응답 처리 및 상태 업데이트
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) response.get("response");

            responseList.stream()
                    .filter(responseData -> {
                        // "league" 데이터에서 round 가져오기
                        Map<String, Object> leagueData = (Map<String, Object>) responseData.get("league");
                        String round = (String) leagueData.get("round");
                        return round.contains("Regular Season"); // Regular Season만 필터링
                    })
                    .map(responseData -> {
                        // "fixture" 데이터 추출
                        Map<String, Object> fixtureData = (Map<String, Object>) responseData.get("fixture");
                        Long fixtureId = Long.valueOf((Integer) fixtureData.get("id"));
                        Map<String, Object> statusData = (Map<String, Object>) fixtureData.get("status");
                        String status = (String) statusData.get("short");

                        // "goals" 데이터 추출
                        Map<String, Object> goalsData = (Map<String, Object>) responseData.get("goals");
                        Integer homeScore = (Integer) goalsData.get("home");
                        Integer awayScore = (Integer) goalsData.get("away");

                        Integer homePenaltyScore = null, awayPenaltyScore = null;
                        if (status.equals("PEN")) {
                            Map<String, Object> scoreData = (Map<String, Object>) responseData.get("score");
                            Map<String, Object> penaltyData = (Map<String, Object>) scoreData.get("penalty");
                            homePenaltyScore = (Integer) penaltyData.get("home");
                            awayPenaltyScore = (Integer) penaltyData.get("away");
                        }

                        // DTO 객체 생성 및 상태 업데이트
                        ApiGamesDTO gameDTO = ApiGamesDTO.builder()
                                .id(fixtureId)
                                .awayScore(awayScore)
                                .homeScore(homeScore)
                                .status(status)
                                .homePenaltyScore(homePenaltyScore)
                                .awayPenaltyScore(awayPenaltyScore)
                                .build();
                        return gameDTO;
                    })
                    .forEach(list::add); // 결과를 리스트에 추가
        }
        return list;
    }
}
