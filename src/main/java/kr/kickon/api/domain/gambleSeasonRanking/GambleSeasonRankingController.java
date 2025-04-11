package kr.kickon.api.domain.gambleSeasonRanking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.actualSeasonRanking.dto.GetActualSeasonRankingDTO;
import kr.kickon.api.domain.actualSeasonRanking.request.GetActualSeasonRankingRequestDTO;
import kr.kickon.api.domain.actualSeasonRanking.response.GetActualSeasonRankingResponse;
import kr.kickon.api.domain.gambleSeason.GambleSeasonService;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.domain.gambleSeasonRanking.response.GetGambleSeasonRankingResponse;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.GambleSeasonRanking;
import kr.kickon.api.global.common.entities.User;
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
@RequestMapping("/api/gamble-season-ranking")
@Tag(name = "승부예측 시즌 랭킹")
@Slf4j
public class GambleSeasonRankingController {
    private final GambleSeasonRankingService gambleSeasonRankingService;
    private final GambleSeasonService gambleSeasonService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "승부예측 시즌 랭킹 리스트 조회", description = "리그 PK값 기준으로 해당 리그의 현재 시즌 랭킹 리스트 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetGambleSeasonRankingResponse.class))),
    })
    @GetMapping()
    public ResponseEntity<ResponseDTO<List<GetGambleSeasonRankingDTO>>> getEventBoards(@Valid GetActualSeasonRankingRequestDTO paramDto) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        GambleSeason gambleSeason = gambleSeasonService.findRecentOperatingSeasonByLeaguePk(paramDto.getLeague());
        List<GambleSeasonRanking> gambleSeasonRankings = gambleSeasonRankingService.findRecentSeasonRankingByLeague(gambleSeason.getPk());
        List<GetGambleSeasonRankingDTO>getGambleSeasonRankingDTOS = gambleSeasonRankings.stream().map(gambleSeasonRanking -> GetGambleSeasonRankingDTO.builder()
                        .rankOrder(gambleSeasonRanking.getRankOrder())
                        .points((double) gambleSeasonRanking.getPoints()/1000)
                        .teamLogoUrl(gambleSeasonRanking.getTeam().getLogoUrl())
                        .teamName(gambleSeasonRanking.getTeam().getNameKr()).build()).toList();

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, getGambleSeasonRankingDTOS));
    }

}
