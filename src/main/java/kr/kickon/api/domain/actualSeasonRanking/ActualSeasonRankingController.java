package kr.kickon.api.domain.actualSeasonRanking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.actualSeasonRanking.dto.GetActualSeasonRankingDTO;
import kr.kickon.api.domain.actualSeasonRanking.request.GetActualSeasonRankingRequestDTO;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/actual-season-ranking")
@Tag(name = "실제 시즌 랭킹")
@Slf4j
public class ActualSeasonRankingController {
    private final ActualSeasonRankingService actualSeasonRankingService;
    private final ActualSeasonService actualSeasonService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "실제 시즌 랭킹 리스트 조회", description = "리그 PK값 기준으로 해당 리그의 현재 시즌 랭킹 리스트 조회")
    @GetMapping()
    public ResponseEntity<ResponseDTO<List<GetActualSeasonRankingDTO>>> getEventBoards(@Valid GetActualSeasonRankingRequestDTO paramDto) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        ActualSeason league = actualSeasonService.findRecentByLeaguePk(paramDto.getLeague());
        List<GetActualSeasonRankingDTO> eventBoards = actualSeasonRankingService.findRecentSeasonRankingByLeague(league.getPk());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, eventBoards));
    }

}
