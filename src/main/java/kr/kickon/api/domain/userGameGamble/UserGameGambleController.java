package kr.kickon.api.domain.userGameGamble;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.game.GameService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.domain.userGameGamble.request.UserGameGamblePatchRequest;
import kr.kickon.api.domain.userGameGamble.request.UserGameGamblePostRequest;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Game;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.entities.UserGameGamble;
import kr.kickon.api.global.common.enums.GambleStatus;
import kr.kickon.api.global.common.enums.PredictedResult;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.error.exceptions.ForbiddenException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-game-gamble")
@Tag(name = "유저 승부예측 참여")
@Slf4j
public class UserGameGambleController {
    private final UserGameGambleService userGameGambleService;
    private final UserFavoriteTeamService userFavoriteTeamService;
    private final GameService gameService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UUIDGenerator uuidGenerator;

    @PostMapping()
    @Operation(summary = "승부예측 생성", description = "게임과 유저를 기반으로 승부예측 생성")
    public ResponseEntity<ResponseDTO<Void>> createUserGameGamble(@Valid @RequestBody UserGameGamblePostRequest request) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        Game game = gameService.findByPk(request.getGame());
        if(game == null) throw new NotFoundException(ResponseCode.NOT_FOUND_GAME);

        // 이미 예측했는지 확인
        UserGameGamble existing = userGameGambleService.findByUserAndGame(user.getPk(), game.getPk());
        if (existing != null) throw new BadRequestException(ResponseCode.DUPLICATED_USER_GAME_GAMBLE);

        // 게임 시작 2시간 전까지만 허용
        if (game.getStartedAt().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException(ResponseCode.GAMBLE_CLOSED);
        }

        // 예측 결과 계산
        PredictedResult result;
        if (request.getPredictedHomeScore().equals(request.getPredictedAwayScore())) {
            result = PredictedResult.DRAW;
        } else if (request.getPredictedHomeScore() > request.getPredictedAwayScore()) {
            result = PredictedResult.HOME;
        } else {
            result = PredictedResult.AWAY;
        }

        String id = uuidGenerator.generateUniqueUUID(userGameGambleService::findById);

        UserGameGamble gamble = UserGameGamble.builder()
                .user(user)
                .game(game)
                .id(id)
                .predictedAwayScore(request.getPredictedAwayScore())
                .predictedHomeScore(request.getPredictedHomeScore())
                .predictedResult(result)
                .build();

        // ✅ 유저가 좋아하는 팀 중 경기 참여 팀을 찾고, priorityNum 기준으로 우선순위 높은 팀 설정
        List<UserFavoriteTeam> favoriteTeams = userFavoriteTeamService.findAllByUserPk(user.getPk());
        favoriteTeams.sort(Comparator.comparingInt(UserFavoriteTeam::getPriorityNum));

        for (UserFavoriteTeam favoriteTeam : favoriteTeams) {
            Long teamPk = favoriteTeam.getTeam().getPk();
            if (teamPk.equals(game.getHomeTeam().getPk()) || teamPk.equals(game.getAwayTeam().getPk())) {
                gamble.setSupportingTeam(favoriteTeam.getTeam());
                break;
            }
        }

        userGameGambleService.save(gamble);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED));
    }

    @PatchMapping()
    @Operation(summary = "승부예측 수정", description = "승부예측 ID를 기반으로 승부예측 수정")
    public ResponseEntity<ResponseDTO<Void>> updateUserGameGamble(@Valid @RequestBody UserGameGamblePatchRequest request) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        UserGameGamble userGameGamble = userGameGambleService.findById(request.getGamble());
        if(userGameGamble == null) throw new NotFoundException(ResponseCode.NOT_FOUND_USER_GAME_GAMBLE);
        if(!userGameGamble.getUser().getPk().equals(user.getPk())) throw new ForbiddenException(ResponseCode.FORBIDDEN);

        // 게임 시작 2시간 전까지만 허용
        // 현재 시간과 게임 시작 시간 비교
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime gameStartTime = userGameGamble.getGame().getStartedAt();

        if (gameStartTime.isBefore(now.plusMinutes(30))) {
            throw new BadRequestException(ResponseCode.GAMBLE_CLOSED);
        }

        String id = uuidGenerator.generateUniqueUUID(userGameGambleService::findById);
        // 승부예측 생성 로직
        PredictedResult result;
        if(request.getPredictedAwayScore().equals(request.getPredictedHomeScore())) result = PredictedResult.DRAW;
        else if(request.getPredictedHomeScore()>request.getPredictedAwayScore()) result = PredictedResult.HOME;
        else result = PredictedResult.AWAY;
        userGameGamble.setPredictedAwayScore(request.getPredictedAwayScore());
        userGameGamble.setPredictedHomeScore(request.getPredictedHomeScore());
        userGameGamble.setPredictedResult(result);

        UserFavoriteTeam userFavoriteTeam = userFavoriteTeamService.findByUserPk(user.getPk());
        if(userFavoriteTeam!=null) userGameGamble.setSupportingTeam(userFavoriteTeam.getTeam());
        userGameGambleService.save(userGameGamble);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

    @DeleteMapping()
    @Operation(summary = "승부예측 삭제", description = "승부예측 ID를 기반으로 승부예측 데이터 hard delete")
    public ResponseEntity<ResponseDTO<Void>> deleteUserGameGamble(@Valid @RequestParam String id) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        UserGameGamble userGameGamble = userGameGambleService.findById(id);
        if(userGameGamble==null) throw new NotFoundException(ResponseCode.NOT_FOUND_USER_GAME_GAMBLE);
        if(!userGameGamble.getUser().getPk().equals(user.getPk())) throw new ForbiddenException(ResponseCode.FORBIDDEN);
        if(!userGameGamble.getGambleStatus().equals(GambleStatus.COMPLETED)) throw new BadRequestException(ResponseCode.ALREADY_FINISHED_GAMBLE);
        // 게임 시작 30분 전까지만 허용
        // 현재 시간과 게임 시작 시간 비교
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime gameStartTime = userGameGamble.getGame().getStartedAt();

        if (gameStartTime.isBefore(now.plusMinutes(30))) {
            throw new BadRequestException(ResponseCode.GAMBLE_CLOSED);
        }
        userGameGambleService.delete(userGameGamble);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
