package kr.kickon.api.domain.migration;

import jakarta.transaction.Transactional;
import kr.kickon.api.domain.migration.dto.ApiTeamDTO;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class MigrationService {
//    private final RestTemplate restTemplate;
//
//
//    public ApiTeamDTO[] fetchTeams(){
//        ApiTeamDTO response = restTemplate.getForObject("", ApiTeamDTO[].class);
//    }
//
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
