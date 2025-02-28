package kr.kickon.api.domain.migration;

import kr.kickon.api.domain.team.TeamService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/migration")
public class MigrationController {
    private final TeamService teamService;
//
//    @PostMapping("/teams")
//    public String fetchTeams(@RequestParam String apiUrl) {
//        teamService.fetchAndSaveTeams(apiUrl);
//        return ;
//    }
}
