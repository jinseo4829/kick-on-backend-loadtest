package kr.kickon.api.domain.league;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.league.response.GetLeaguesResponse;
import kr.kickon.api.domain.news.dto.NewsListDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/league")
@Tag(name = "리그")
@Slf4j
public class LeagueController {
    private final LeagueService leagueService;
    @Operation(summary = "리그 리스트 조회", description = "현재 서비스 제공 가능한 리그 리스트 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetLeaguesResponse.class))),
    })
    @GetMapping()
    public ResponseEntity<ResponseDTO<List<LeagueDTO>>> getHomeNews() {
        List<League> leagues = leagueService.findAll();
        List<LeagueDTO> leagueData = leagues.stream().map(league ->
            LeagueDTO.builder()
                    .pk(league.getPk())
                    .leagueType(league.getType())
                    .enName(league.getEnName())
                    .krName(league.getKrName())
                    .logoUrl(league.getLogoUrl())
                    .build()
        ).toList();
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, leagueData));
    }
}
