package kr.kickon.api.admin.team;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.kickon.api.admin.team.dto.TeamDetailDTO;
import kr.kickon.api.admin.team.dto.TeamListDTO;
import kr.kickon.api.admin.team.request.UpdateTeamRequest;
import kr.kickon.api.admin.team.request.TeamFilterRequest;
import kr.kickon.api.admin.team.response.GetTeamDetailResponse;
import kr.kickon.api.admin.team.response.GetTeamsResponse;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/team")
@Tag(name = "팀 관리")
@Slf4j
public class AdminTeamController {

  private final AdminTeamService adminTeamService;
  @GetMapping
  @Operation(summary = "팀 리스트 조회", description = "팀 리스트를 조회합니다. 각 filter 조건은 옵셔널 입니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetTeamsResponse.class))),
  })
  public ResponseEntity<ResponseDTO<List<TeamListDTO>>> getFilteredTeams(@Valid @ModelAttribute TeamFilterRequest request) {
    Pageable pageable = request.toPageable();
    Page<TeamListDTO> TeamPage = adminTeamService.getTeamListByFilter(request, pageable);

    return ResponseEntity.ok(
        ResponseDTO.success(
            ResponseCode.SUCCESS,
            TeamPage.getContent(),
            new PagedMetaDTO(
                TeamPage.getNumber() + 1,
                TeamPage.getSize(),
                TeamPage.getTotalElements()
            )
        )
    );
  }

  @GetMapping("/{pk}")
  @Operation(summary = "팀 상세 조회", description = "pk값으로 팀 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetTeamDetailResponse.class))),
  })
  public ResponseEntity<ResponseDTO<TeamDetailDTO>> getTeamDetail(@PathVariable Long pk) {
    Team team = adminTeamService.findByPk(pk);
    if (team == null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
    TeamDetailDTO dto = adminTeamService.getTeamDetail(team);
    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, dto));
  }

  @PatchMapping("/{pk}")
  @Operation(summary = "팀 수정", description = "pk값으로 팀을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetTeamDetailResponse.class))),
  })
  public ResponseEntity<ResponseDTO<TeamDetailDTO>> patchTeam(@PathVariable Long pk,
      @RequestBody UpdateTeamRequest request) {
    Team team = adminTeamService.findByPk(pk);
    if (team == null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
    TeamDetailDTO responseDto = adminTeamService.updateTeam(team, request);
    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, responseDto));
  }
}
