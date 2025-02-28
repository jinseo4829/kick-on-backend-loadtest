package kr.kickon.api.domain.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.migration.dto.ApiLeagueAndSeasonDTO;
import kr.kickon.api.domain.migration.dto.ApiLeagueDTO;
import kr.kickon.api.domain.migration.dto.ApiSeasonDTO;
import kr.kickon.api.domain.migration.dto.ApiTeamDTO;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public MigrationService(@Value("${api.key}") String apiKey, LeagueService leagueService, TeamService teamService, ActualSeasonService actualSeasonService, UUIDGenerator uuidGenerator, ActualSeasonTeamService actualSeasonTeamService) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://v3.football.api-sports.io")
                .defaultHeader("x-rapidapi-host","v3.football.api-sports.io")
                .defaultHeader("x-rapidapi-key", apiKey)
                .build();
        this.leagueService = leagueService;
        this.teamService = teamService;
        this.actualSeasonService = actualSeasonService;
        this.uuidGenerator = uuidGenerator;
        this.actualSeasonTeamService = actualSeasonTeamService;
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
//            log.error("------------------");
//            log.error(league.getEnName());
//            log.error(actualSeason.getId() + " " + actualSeason.getOperatingStatus() + " " + actualSeason.getLeague() + " " + actualSeason.getStartedAt());
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
