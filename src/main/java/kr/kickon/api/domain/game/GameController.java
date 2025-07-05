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
import kr.kickon.api.domain.game.response.CalendarDateDTO;
import kr.kickon.api.domain.game.response.GetGamesResponse;
import kr.kickon.api.domain.game.response.LeagueWithGamesDTO;
import kr.kickon.api.domain.game.response.NextGameDateDTO;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        // LocalDate → LocalDateTime 변환
        LocalDateTime fromDateTime = (paramDto.getFrom() != null) ? paramDto.getFrom().atStartOfDay() : null;
        LocalDateTime toDateTime = (paramDto.getTo() != null) ? paramDto.getTo().atTime(23,59,59) : null;

        List<Game> games = List.of();

        if (user == null) {
            if(paramDto.getLeague()!= null){
                ActualSeason actualSeason = actualSeasonService.findRecentByLeaguePk(paramDto.getLeague());
                if(actualSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
                games = gameService.findByActualSeason(actualSeason.getPk(), paramDto.getStatus(), fromDateTime, toDateTime);
            }
        } else {
            List<UserFavoriteTeam> favoriteTeams = userFavoriteTeamService.findAllByUserPk(user.getPk());
            games = new ArrayList<>();

            if (favoriteTeams == null || favoriteTeams.isEmpty()) {
                System.out.println("좋아하는 팀 없음");
                if(paramDto.getLeague()!= null){
                    ActualSeason actualSeason = actualSeasonService.findRecentByLeaguePk(paramDto.getLeague());
                    if(actualSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
                    games = gameService.findByActualSeason(actualSeason.getPk(), paramDto.getStatus(), fromDateTime, toDateTime);
                }
            } else {
                if(paramDto.getTeam()!=null){
                    System.out.println("team 넣음");
                    ActualSeasonTeam actualSeasonTeam = actualSeasonTeamService.findLatestByTeam(paramDto.getTeam());
                    if (actualSeasonTeam != null) {
                        List<Game> teamGames = gameService.findByActualSeasonByFavoriteTeam(
                                actualSeasonTeam.getActualSeason().getPk(),
                                paramDto.getStatus(),
                                paramDto.getTeam(),
                                4,
                                fromDateTime,
                                toDateTime
                        );
                        games.addAll(teamGames);
                    }
                }else{
                    System.out.println("team 안 넣음");
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
                                    limitPerTeam,
                                    fromDateTime,
                                    toDateTime
                            );
                            games.addAll(teamGames);
                        }
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
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, LeagueWithGamesDTO.builder()
                .games(gameDTOs)
                .build()));
    }

    @Operation(
            summary = "달력에 경기 일정 표시",
            description = "특정 리그와 월(month) 기준으로, 해당 월에 경기 있는 날짜 리스트를 반환"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = CalendarDateDTO.class))),
    })
    @GetMapping("/calendar")
    public ResponseEntity<ResponseDTO<CalendarDateDTO>> getCalendarDates(
            @RequestParam Long league,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month
    ) {
        List<LocalDate> dates = gameService.getCalendarDates(league, month);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, new CalendarDateDTO(dates)));
    }

    @Operation(
            summary = "가장 가까운 예정 경기 날짜 조회",
            description = "아직 끝나지 않은 경기(PENDING, POSTPONED, PROCEEDING) 중에서 가장 가까운 경기 날짜 반환"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = NextGameDateDTO.class))),
    })
    @GetMapping("/calendar/next")
    public ResponseEntity<ResponseDTO<NextGameDateDTO>> getNextAvailableGameDate(
            @RequestParam Long league,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate after
    ) {
        LocalDate nextDate = gameService.getNextAvailableGameDate(league, after);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, new NextGameDateDTO(nextDate)));
    }

    @Operation(
            summary = "현재 승부예측 오픈 기간 조회",
            description = "오늘 기준으로 승부예측 오픈된 4주 기간(일요일 기준) 반환"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = PredictOpenDTO.class))),
    })
    @GetMapping("/predict/open")
    public ResponseEntity<ResponseDTO<PredictOpenDTO>> getPredictOpenPeriod() {
        LocalDate today = LocalDate.now();
        PredictOpenDTO period = gameService.getPredictOpenPeriod(today);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, period));
    }


}
