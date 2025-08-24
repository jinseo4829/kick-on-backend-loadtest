package kr.kickon.api.domain.team;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.team.request.TeamListFilterRequest;
import kr.kickon.api.domain.team.response.GetTeamsResponse;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
@Tag(name = "팀 관련")
@Slf4j
public class TeamController {
    private final TeamService teamService;

    @Operation(summary = "팀 리스트 조회", description = "리그 pk 기반으로 팀 리스트 조회 / league만 보내면 리그 기준으로 모든 팀 조회, keyword를 보내면 검색어 기반 팀 검색")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetTeamsResponse.class)))
    })
    @GetMapping()
    public ResponseEntity<ResponseDTO<List<TeamDTO>>> getTeams(
        @ModelAttribute TeamListFilterRequest request
    ) {
        Long league = request.getLeague();
        String keyword = request.getKeyword();
        if(league==null && keyword==null) throw new BadRequestException(ResponseCode.INVALID_REQUEST);
        Pageable pageable = request.toPageable();
        Page<TeamDTO> resultPage = teamService.getTeamListByFilter(request, pageable);

        return ResponseEntity.ok(ResponseDTO.success(
            ResponseCode.SUCCESS,
            resultPage.getContent(),
            new PagedMetaDTO(
                resultPage.getNumber() + 1,
                resultPage.getSize(),
                resultPage.getTotalElements()
            )
        ));
    }
}
