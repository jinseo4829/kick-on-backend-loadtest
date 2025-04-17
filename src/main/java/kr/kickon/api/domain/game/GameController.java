package kr.kickon.api.domain.game;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.game.dto.*;
import kr.kickon.api.domain.game.request.GetGamesRequestDTO;
import kr.kickon.api.domain.game.response.GetGamesResponse;
import kr.kickon.api.domain.game.response.LeagueWithGamesDTO;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.domain.userGameGamble.UserGameGambleService;
import kr.kickon.api.domain.userGameGamble.dto.UserGameGambleDTO;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.PredictedResult;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game")
@Tag(name = "매치")
@Slf4j
public class GameController {
    private final GameService gameService;
    private final UserGameGambleService userGameGambleService;
    private final ActualSeasonService actualSeasonService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserFavoriteTeamService userFavoriteTeamService;
    private final ActualSeasonTeamService actualSeasonTeamService;

    @Operation(summary = "매치 리스트 조회", description = "상태값, 리그 pk 기준으로 매치 리스트 조회 / 유저 참여 여부도 포함하여 전달")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetGamesResponse.class))),
    })
    @GetMapping()
    public ResponseEntity<ResponseDTO<LeagueWithGamesDTO>> getGames(@Valid GetGamesRequestDTO paramDto) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        ActualSeason actualSeason = actualSeasonService.findRecentByLeaguePk(paramDto.getLeague());
//        System.out.println(actualSeason);
        if(actualSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
        UserFavoriteTeam userFavoriteTeam = null;
        List<Game> games = null;
        if(user!=null) userFavoriteTeam = userFavoriteTeamService.findByUserPk(user.getPk());
//        System.out.println(userFavoriteTeam.getTeam().getNameKr());
        if(userFavoriteTeam==null) {
            games = gameService.findByActualSeason(actualSeason.getPk(), paramDto.getStatus());
        } else{
            ActualSeasonTeam actualSeasonTeam = actualSeasonTeamService.findByActualSeasonTeam(actualSeason,userFavoriteTeam.getTeam().getPk());
//            System.out.println(actualSeasonTeam.getActualSeason().getLeague());
            if(actualSeasonTeam==null) games = gameService.findByActualSeason(actualSeason.getPk(), paramDto.getStatus());
            else games = gameService.findByActualSeasonByFavoriteTeam(actualSeason.getPk(), paramDto.getStatus(), userFavoriteTeam.getTeam().getPk());
        }

        // 게임 DTO 리스트 변환
        List<GameDTO> gameDTOs = games.stream().map(game -> {
            // 홈팀과 어웨이팀 정보 설정
            TeamDTO homeTeamDTO = new TeamDTO(game.getHomeTeam());

            TeamDTO awayTeamDTO = new TeamDTO(game.getAwayTeam());

            // 승부예측 현황

            Map<PredictedResult, Long> userGamblePredictedResult = userGameGambleService.findGambleCountByGamePk(game.getPk());
            long homeCount = userGamblePredictedResult.getOrDefault(PredictedResult.HOME, 0L);
            long awayCount = userGamblePredictedResult.getOrDefault(PredictedResult.AWAY, 0L);
            long drawCount = userGamblePredictedResult.getOrDefault(PredictedResult.DRAW, 0L);

            long totalParticipation = homeCount + awayCount + drawCount;

            // 0으로 나누는 것을 방지
            int homeRatio = (totalParticipation > 0) ? (int) ((homeCount * 100) / totalParticipation) : 0;
            int awayRatio = (totalParticipation > 0) ? (int) ((awayCount * 100) / totalParticipation) : 0;
            int drawRatio = (totalParticipation > 0) ? (int) ((drawCount * 100) / totalParticipation) : 0;
            GambleResultDTO gambleResultDTO = new GambleResultDTO(homeRatio,awayRatio,drawRatio,totalParticipation);

            GameDTO gameDTO = new GameDTO(homeTeamDTO, awayTeamDTO, game, gambleResultDTO);
            if(user != null && user.getPk() > 0){
                // 유저 승부 예측 결과
                UserGameGambleDTO myGambleResultDTO = null;
                UserGameGamble myUserGameGamble = userGameGambleService.findByUserAndGame(user.getPk(),game.getPk());;
                if (myUserGameGamble != null) {
                    myGambleResultDTO = new UserGameGambleDTO(myUserGameGamble);
                }
                gameDTO.setMyGambleResult(myGambleResultDTO);
            }
            return gameDTO;
        }).collect(Collectors.toList());
        League league = actualSeason.getLeague();
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, LeagueWithGamesDTO.builder()
                .league(new LeagueDTO(league))
                .games(gameDTOs)
                .build()));
    }

}
