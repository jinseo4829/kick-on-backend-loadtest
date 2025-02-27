package kr.kickon.api.domain.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kr.kickon.api.domain.migration.dto.ApiTeamDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MigrationService {
    private final String apiKey;
    private final WebClient webClient;

    public MigrationService(@Value("${api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://v3.football.api-sports.io")
                .defaultHeader("x-rapidapi-host","v3.football.api-sports.io")
                .defaultHeader("x-rapidapi-key", apiKey)
                .build();
    }

    public List<ApiTeamDTO> fetchTeams(int id, int season){
        Map<String, Object> response = webClient.get().uri(uriBuilder ->
                uriBuilder.path("/teams")
                        .queryParam("league",id)
                        .queryParam("season",season)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> responseList = (List<Map<String, Object>>) response.get("response");
        log.error(responseList.get(0).toString());
        List<ApiTeamDTO> teams = responseList.stream()
                .map(responseData -> {
                    Object data = responseData.get("team");
                    ApiTeamDTO apiTeamDTO = null;

                    // Map을 ApiTeamDTO로 변환
                    if (data instanceof Map) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        apiTeamDTO = objectMapper.convertValue(data, ApiTeamDTO.class);
                    }

                    return apiTeamDTO;
                })
                .collect(Collectors.toList());

        log.error(teams.get(0).toString());
        return teams;
    }

//    @Transactional
//    public void fetchTeamsAndSaveTeams() {
//        ApiTeamDTO response = restTemplate.getForObject(apiUrl, ApiTeamDTO.class);
//        if (response != null && response.getTeams() != null) {
//            List<Team> teams = response.getTeams().stream().map(apiTeam -> {
//                Team team = new Team();
//                team.teamId = apiTeam.getId();
//                team.teamName = apiTeam.getName();
//                team.teamLogo = apiTeam.getLogo();
//                team.teamCode = apiTeam.getCode();
//                return team;
//            }).toList();
//            teamRepository.saveAll(teams);
//        }
//    }
}
