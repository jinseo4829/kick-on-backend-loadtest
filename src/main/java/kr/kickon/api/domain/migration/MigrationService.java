package kr.kickon.api.domain.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.game.GameService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.migration.dto.*;
import kr.kickon.api.domain.team.TeamService;
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

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MigrationService {
    private final String apiKey;
    private final WebClient webClient;
    private final LeagueService leagueService;
    private final TeamService teamService;
    private final ActualSeasonService actualSeasonService;
    private final UUIDGenerator uuidGenerator;
    private final ActualSeasonTeamService actualSeasonTeamService;
    private final GameService gameService;

    public MigrationService(@Value("${api.key}") String apiKey, LeagueService leagueService, TeamService teamService, ActualSeasonService actualSeasonService, UUIDGenerator uuidGenerator, ActualSeasonTeamService actualSeasonTeamService, GameService gameService) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)) // to unlimited memory size
                .build();
        this.apiKey = apiKey;
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
    }

    @Transactional
    public void saveGames(List<ApiGamesDTO> list){
        // 게임 아이디 체크
        List<String> gameIds = new ArrayList<>();
        list.forEach(apiData -> {
            try{
                // 필수 값 체크
                gameService.findByApiId(apiData.getId());
            }catch (NotFoundException ignore){
                Optional<Team> homeTeam, awayTeam;
                homeTeam = teamService.findByApiId(apiData.getHomeTeamId());
                awayTeam = teamService.findByApiId(apiData.getAwayTeamId());
                if(homeTeam.isEmpty() || awayTeam.isEmpty()){}
                else{
                    Game game;
                    List<String> scheduledStatus = new ArrayList<>(Arrays.asList(GameService.ScheduledStatus));
                    List<String> finishedStatus = new ArrayList<>(Arrays.asList(GameService.FinishedStatus));
                    GameStatus gameStatus;
                    String gameId = "";

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
                    gameService.save(game);
                }

            }
        });
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

            try {
                actualSeason = actualSeasonService.findByYearAndLeague(apiSeason.getYear(),league.getPk());
                actualSeason.setYear(apiSeason.getYear());
                actualSeason.setStartedAt(apiSeason.getStart());
                actualSeason.setFinishedAt(apiSeason.getEnd());
                actualSeason.setFinishedAt(apiSeason.getEnd());
                apiSeason.setOperatingStatus(apiSeason.getOperatingStatus());
                actualSeasonService.save(actualSeason);
            }catch (NotFoundException e) {
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
            }
        });
    }

    @Transactional
    public void saveTeamsAndSeasonTeams(List<ApiTeamDTO> apiTeams){
        List<String> ids = new ArrayList<>();
        List<String> actualSeasonTeamIds = new ArrayList<>();

        apiTeams.forEach(apiTeam -> {
            String id="";

            Optional<Team> team = teamService.findByApiId(apiTeam.getId());
            Team teamObj;

            if(team.isPresent()) {
                teamObj = team.get();
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
            try {
                actualSeasonTeamService.findByActualSeason(actualSeason,teamObj.getPk());
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

    public List<ApiGamesDTO> fetchGames(List<League> leagues, String season){
        List<ApiGamesDTO> list = new ArrayList<>();
        for(League league : leagues) {
            ActualSeason actualSeason = actualSeasonService.findByYearAndLeague(Integer.parseInt(season),league.getPk());
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
}
