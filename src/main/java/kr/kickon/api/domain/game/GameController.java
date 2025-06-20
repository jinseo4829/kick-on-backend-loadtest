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
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
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

import java.util.ArrayList;
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

        List<Game> games;
        ActualSeason actualSeason = actualSeasonService.findRecentByLeaguePk(paramDto.getLeague());
        if(actualSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
        if (user == null) {
            games = gameService.findByActualSeason(actualSeason.getPk(), paramDto.getStatus());
        } else {
            List<UserFavoriteTeam> favoriteTeams = userFavoriteTeamService.findAllByUserPk(user.getPk());
            games = new ArrayList<>();

            if (favoriteTeams == null || favoriteTeams.isEmpty()) {
                games = gameService.findByActualSeason(actualSeason.getPk(), paramDto.getStatus());
            } else {
                int teamCount = favoriteTeams.size();
                int limitPerTeam;

                if ("finished".equalsIgnoreCase(paramDto.getStatus())) {
                    limitPerTeam = 1;
                } else { // proceeding
                    limitPerTeam = (teamCount <= 2) ? 2 : 1;
                }

                for (UserFavoriteTeam favoriteTeam : favoriteTeams) {
                    ActualSeasonTeam actualSeasonTeam = actualSeasonTeamService.findLatestByTeam(favoriteTeam.getTeam().getPk());
                    if (actualSeasonTeam != null) {
                        List<Game> teamGames = gameService.findByActualSeasonByFavoriteTeam(
                                actualSeasonTeam.getActualSeason().getPk(),
                                paramDto.getStatus(),
                                favoriteTeam.getTeam().getPk(),
                                limitPerTeam
                        );
                        games.addAll(teamGames);
                    }
                }
            }
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
            gameDTO.setLeague(new LeagueDTO(game.getActualSeason().getLeague()));
            return gameDTO;
        }).collect(Collectors.toList());
        League league = actualSeason.getLeague();
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, LeagueWithGamesDTO.builder()
                .league(new LeagueDTO(league))
                .games(gameDTOs)
                .build()));
    }

}
