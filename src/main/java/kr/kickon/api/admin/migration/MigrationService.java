package kr.kickon.api.admin.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kr.kickon.api.admin.migration.dto.*;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.actualSeasonRanking.ActualSeasonRankingService;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.gambleSeason.GambleSeasonService;
import kr.kickon.api.domain.gambleSeasonPoint.GambleSeasonPointService;
import kr.kickon.api.domain.gambleSeasonRanking.GambleSeasonRankingService;
import kr.kickon.api.domain.gambleSeasonTeam.GambleSeasonTeamService;
import kr.kickon.api.domain.game.GameService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.userGameGamble.UserGameGambleService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.GameStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static kr.kickon.api.domain.game.GameService.getGameStatus;

@Service
@Slf4j
public class MigrationService {
    private final WebClient webClient;
    private final ActualSeasonRankingService actualSeasonRankingService;
    private final LeagueService leagueService;
    private final TeamService teamService;
    private final ActualSeasonService actualSeasonService;
    private final ActualSeasonTeamService actualSeasonTeamService;
    private final GameService gameService;
    private final UserGameGambleService userGameGambleService;
    private final GambleSeasonPointService gambleSeasonPointService;
    private final GambleSeasonRankingService gambleSeasonRankingService;
    private final GambleSeasonService gambleSeasonService;
    private final GambleSeasonTeamService gambleSeasonTeamService;

    public MigrationService(@Value("${api.key}") String apiKey, LeagueService leagueService, TeamService teamService, ActualSeasonService actualSeasonService, UUIDGenerator uuidGenerator, ActualSeasonTeamService actualSeasonTeamService, GameService gameService, ActualSeasonRankingService actualSeasonRankingService, UserGameGambleService userGameGambleService, GambleSeasonPointService gambleSeasonPointService, GambleSeasonRankingService gambleSeasonRankingService, GambleSeasonService gambleSeasonService, GambleSeasonTeamService gambleSeasonTeamService) {
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
        this.actualSeasonTeamService = actualSeasonTeamService;
        this.gameService = gameService;
        this.actualSeasonRankingService = actualSeasonRankingService;
        this.userGameGambleService = userGameGambleService;
        this.gambleSeasonPointService = gambleSeasonPointService;
        this.gambleSeasonRankingService = gambleSeasonRankingService;
        this.gambleSeasonTeamService = gambleSeasonTeamService;
    }

    @Transactional
    public void saveRankings(List<ApiRankingDTO> list) {
        list.forEach(apiData -> {
            ActualSeasonRanking existActualSeasonRanking = actualSeasonRankingService.findByActualSeasonAndTeam(apiData.getActualSeason().getPk(),apiData.getTeam().getPk());

            ActualSeasonRanking actualSeasonRanking;
            if(existActualSeasonRanking == null) {
                actualSeasonRanking = ActualSeasonRanking.builder()
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
                    game = Game.builder()
                            .id(UUID.randomUUID().toString())
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

    @Transactional
    public void saveLeagueAndSeason(List<ApiLeagueAndSeasonDTO> list){
        list.forEach(apiData -> {
            ApiLeagueDTO apiLeague = apiData.getLeague();
            ApiSeasonDTO apiSeason = apiData.getSeason();
            League league;
            ActualSeason actualSeason;
            try {
                league = leagueService.findByApiId(apiLeague.getId());
                league.setNameEn(apiLeague.getName());
                league.setType(apiLeague.getType());
                league.setLogoUrl(apiLeague.getLogo());
                leagueService.save(league);
            }catch (NotFoundException e) {
                league = League.builder()
                        .apiId(apiLeague.getId())
                        .type(apiLeague.getType())
                        .logoUrl(apiLeague.getLogo())
                        .build();
                leagueService.save(league);
            }
            actualSeason = actualSeasonService.findByYearAndLeague(apiSeason.getYear(),league.getPk());
            if(actualSeason == null) {
                actualSeason = ActualSeason.builder()
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
        List<String> actualSeasonTeamIds = new ArrayList<>();

        apiTeams.forEach(apiTeam -> {

            Team team = teamService.findByApiId(apiTeam.getId());
            Team teamObj;

            if(team != null) {
                teamObj = team;
                teamObj.setCode(apiTeam.getCode());
                teamObj.setNameEn(apiTeam.getName());
                teamObj.setLogoUrl(apiTeam.getLogo());
                teamObj = teamService.save(teamObj);
            }else{
                teamObj = Team.builder()
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
                ActualSeasonTeam actualSeasonObj = ActualSeasonTeam.builder()
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
//            log.error("{}",actualSeason.getYear());
//            log.error("{}",league.getNameKr());
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
//            log.error("{}",responseData);
            if(responseData.isEmpty()) continue;
            Map<String, Object> leagueData = (Map<String, Object>) responseData.get(0).get("league");
            List<List<Map<String, Object>>> rankingDatas = (List<List<Map<String, Object>>>) leagueData.get("standings");
            List<Map<String, Object>> flattenedList = rankingDatas.stream().flatMap(List::stream)
                            .collect(Collectors.toList());
            list.addAll(flattenedList.stream()
                    .filter(rankingData -> {
                        String group = (String) rankingData.get("group");
                        return group != null && (group.contains("Regular Season") || group.replaceAll(" ","").equals(((String) leagueData.get("name")).replaceAll(" ","")));
                    })
                    .map(rankingData -> {
                        // DTO 객체 생성
                        Map<String, Object> teamData = (Map<String, Object>) rankingData.get("team");
                        Map<String, Object> metaData = (Map<String, Object>) rankingData.get("all");
                        Map<String, Object> goalData = (Map<String, Object>) metaData.get("goals");
//                        log.error(rankingData.toString());

                        log.info("api id : {}", teamData.get("id").toString());
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
//        log.error("{}",list.size());
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
                .toList();
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
                            System.out.println(apiLeagueDTO);
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
                    .toList());
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
                                .homeTeamId(homeTeamId)
                                .awayTeamId(awayTeamId)
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

    public void updateFinalTeamRanking() {
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
}
