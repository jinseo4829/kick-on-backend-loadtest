package kr.kickon.api.domain.game;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.actualSeasonRanking.request.GetActualSeasonRankingRequestDTO;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.gambleSeason.GambleSeasonService;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.domain.gambleSeasonRanking.response.GetGambleSeasonRankingResponse;
import kr.kickon.api.domain.game.dto.*;
import kr.kickon.api.domain.game.request.GetGamesRequestDTO;
import kr.kickon.api.domain.game.response.GetGamesResponse;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.domain.userGameGamble.UserGameGambleService;
import kr.kickon.api.domain.userGameGamble.dto.GambleCountDTO;
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
    public ResponseEntity<ResponseDTO<LeagueDTO>> getGames(@Valid GetGamesRequestDTO paramDto) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        ActualSeason actualSeason = actualSeasonService.findByYearAndLeague(paramDto.getSeason(),paramDto.getLeague());
        if(actualSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
        LeagueDTO leagueDTO = new LeagueDTO();
        leagueDTO.setPk(actualSeason.getLeague().getPk());
        leagueDTO.setName(actualSeason.getLeague().getKrName());
        UserFavoriteTeam userFavoriteTeam = null;
        List<Game> games = null;
        if(user!=null) userFavoriteTeam = userFavoriteTeamService.findByUserPk(user.getPk());

        if(userFavoriteTeam==null) {
            games = gameService.findByActualSeason(actualSeason.getPk(), paramDto.getStatus());
        } else{
            ActualSeasonTeam actualSeasonTeam = actualSeasonTeamService.findByActualSeasonTeam(actualSeason,userFavoriteTeam.getTeam().getPk());
            if(actualSeasonTeam==null) games = gameService.findByActualSeason(actualSeason.getPk(), paramDto.getStatus());
            else games = gameService.findByActualSeasonByFavoriteTeam(actualSeason.getPk(), paramDto.getStatus(), userFavoriteTeam.getTeam().getPk());
        }

        // 게임 DTO 리스트 변환
        List<GameDTO> gameDTOs = games.stream().map(game -> {
            GameDTO gameDTO = new GameDTO();

            // 홈팀과 어웨이팀 정보 설정
            TeamDTO homeTeamDTO = new TeamDTO();
            homeTeamDTO.setPk(game.getHomeTeam().getPk());
            homeTeamDTO.setName(game.getHomeTeam().getNameKr());
            homeTeamDTO.setLogoUrl(game.getHomeTeam().getLogoUrl());
            gameDTO.setHomeTeam(homeTeamDTO);

            TeamDTO awayTeamDTO = new TeamDTO();
            awayTeamDTO.setPk(game.getAwayTeam().getPk());
            awayTeamDTO.setName(game.getAwayTeam().getNameKr());
            awayTeamDTO.setLogoUrl(game.getAwayTeam().getLogoUrl());
            gameDTO.setAwayTeam(awayTeamDTO);


            // 경기 관련 정보 설정
            gameDTO.setGameStatus(game.getGameStatus());
            gameDTO.setStartAt(game.getStartedAt());
            gameDTO.setAwayPenaltyScore(game.getAwayPenaltyScore());
            gameDTO.setHomePenaltyScore(game.getHomePenaltyScore());
            gameDTO.setRound(game.getRound());
            gameDTO.setHomeScore(game.getHomeScore());
            gameDTO.setAwayScore(game.getAwayScore());
            gameDTO.setPk(game.getPk());

            // 승부예측 현황
            GambleResultDTO gambleResultDTO = new GambleResultDTO();
            Map<PredictedResult, Long> userGamblePredictedResult = userGameGambleService.findGambleCountByGamePk(game.getPk());
            long homeCount = userGamblePredictedResult.getOrDefault(PredictedResult.HOME, 0L);
            long awayCount = userGamblePredictedResult.getOrDefault(PredictedResult.AWAY, 0L);
            long drawCount = userGamblePredictedResult.getOrDefault(PredictedResult.DRAW, 0L);

            long totalParticipation = homeCount + awayCount + drawCount;

            // 0으로 나누는 것을 방지
            int homeRatio = (totalParticipation > 0) ? (int) ((homeCount * 100) / totalParticipation) : 0;
            int awayRatio = (totalParticipation > 0) ? (int) ((awayCount * 100) / totalParticipation) : 0;
            int drawRatio = (totalParticipation > 0) ? (int) ((drawCount * 100) / totalParticipation) : 0;
            gambleResultDTO.setHome(homeRatio);
            gambleResultDTO.setAway(awayRatio);
            gambleResultDTO.setDraw(drawRatio);
            gambleResultDTO.setParticipationNumber(totalParticipation);
            gameDTO.setGambleResult(gambleResultDTO);

            if(user != null && user.getPk() > 0){
                // 유저 승부 예측 결과
                MyGambleResultDTO myGambleResultDTO = null;
                UserGameGamble myUserGameGamble = userGameGambleService.findByUserAndGame(user.getPk(),game.getPk());;
                if (myUserGameGamble != null) {
                    myGambleResultDTO = MyGambleResultDTO
                            .builder()
                            .id(myUserGameGamble.getId())
                            .homeScore(myUserGameGamble.getPredictedHomeScore())
                            .awayScore(myUserGameGamble.getPredictedAwayScore())
                            .result(myUserGameGamble.getPredictedResult())
                            .gambleStatus(myUserGameGamble.getGambleStatus()).build();
                }
                gameDTO.setMyGambleResult(myGambleResultDTO);
            }
            return gameDTO;
        }).collect(Collectors.toList());

        // 게임 리스트를 DTO에 설정
        leagueDTO.setGames(gameDTOs);

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, leagueDTO));
    }

}
