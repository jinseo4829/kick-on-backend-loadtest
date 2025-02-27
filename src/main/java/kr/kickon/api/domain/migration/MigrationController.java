package kr.kickon.api.domain.migration;

import io.swagger.v3.oas.annotations.Operation;
import kr.kickon.api.domain.migration.dto.ApiTeamDTO;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/migration")
public class MigrationController {
    private final TeamService teamService;
    private final MigrationService migrationService;

    @Operation(summary = "팀 불러오기", description = "각 리그 별로 속한 팀 불러오기")
    @PostMapping("/teams")
    public ResponseEntity<ResponseDTO<Void>> fetchTeams(@RequestParam String league,@RequestParam String season) {
        List<ApiTeamDTO> teams = migrationService.fetchTeams(Integer.parseInt(league),Integer.parseInt(season));
        teamService.saveTeamsByApi(teams);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED));
    }

//    @Operation(summary = "리그 및 시즌 불러오기", description = "각 리그와 시즌 불러오기")
//    @PostMapping("/teams")
//    public ResponseEntity<ResponseDTO<Void>> fetchTeams(@RequestParam String league,@RequestParam String season) {
//        List<ApiTeamDTO> teams = migrationService.fetchTeams(Integer.parseInt(league),Integer.parseInt(season));
//        teamService.saveTeamsByApi(teams);
//        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED));
//    }
}
