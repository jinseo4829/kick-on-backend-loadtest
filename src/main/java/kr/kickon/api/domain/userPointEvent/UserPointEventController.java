package kr.kickon.api.domain.userPointEvent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.gambleSeason.GambleSeasonService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.domain.userPointEvent.dto.UserRankingDTO;
import kr.kickon.api.domain.userPointEvent.response.GetUserPointRankingResponse;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-point-event")
@Tag(name = "유저 포인트 이벤트")
@Slf4j
public class UserPointEventController {
    private final UserPointEventService userPointEventService;
    private final JwtTokenProvider  jwtTokenProvider;

    @Operation(summary = "유저 랭킹 조회", description = "응원팀 여부에 따라 있으면 응원팀 내 랭킹 반환, 없다면 전체 기준 랭킹 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetUserPointRankingResponse.class))),
    })
    @GetMapping("ranking")
    public ResponseEntity<ResponseDTO<UserRankingDTO>> getUserPointRanking() {
        User user = jwtTokenProvider.getUserFromSecurityContext();

        // 기준 기간: 최근 3개월
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeMonthsAgo = now.minusMonths(3);

        UserRankingDTO rankingDTO = userPointEventService.findUserRankingFromAll(threeMonthsAgo, now, user.getPk());

        if (rankingDTO == null) {
            throw new NotFoundException(ResponseCode.NOT_FOUND_USER_POINT_RANKING);
        }

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, rankingDTO));
    }
}
