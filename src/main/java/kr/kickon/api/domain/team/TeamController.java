package kr.kickon.api.domain.team;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.team.response.GetTeamsResponseDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
@Tag(name = "팀 관련")
@Slf4j
public class TeamController {
    private final ActualSeasonService actualSeasonService;
    private final ActualSeasonTeamService actualSeasonTeamService;
    private final TeamService teamService;

    @Operation(summary = "팀 리스트 조회", description = "리그 pk 기반으로 팀 리스트 조회 / league만 보내면 리그 기준으로 모든 팀 조회, keyword를 보내면 검색어 기반 팀 검색")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetTeamsResponseDTO.class)))
    })
    @GetMapping()
    public ResponseEntity<ResponseDTO<List<TeamDTO>>> getTeams(@RequestParam(required = false) Long league, @RequestParam(required = false) String keyword) {
        if(league==null && keyword==null) throw new BadRequestException(ResponseCode.INVALID_REQUEST);
        if(league!=null){
            ActualSeason actualSeason = actualSeasonService.findRecentByLeaguePk(league);
            if(actualSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
            List<ActualSeasonTeam> actualSeasonTeamList = actualSeasonTeamService.findByActualSeason(actualSeason.getPk(), keyword);
            List<TeamDTO> teamDTOList = actualSeasonTeamList.stream().map(actualSeasonTeam -> (TeamDTO) TeamDTO.builder()
                    .pk(actualSeasonTeam.getTeam().getPk())
                    .nameKr(actualSeasonTeam.getTeam().getNameKr())
                    .nameEn(actualSeasonTeam.getTeam().getNameEn())
                    .logoUrl(actualSeasonTeam.getTeam().getLogoUrl())
                    .build()).toList();
            return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, teamDTOList));
        }else{
            List<Team> teams = teamService.findByKeyword(keyword);
            List<TeamDTO> teamDTOList = teams.stream().map(TeamDTO::new).toList();
            return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, teamDTOList));
        }
    }
}
