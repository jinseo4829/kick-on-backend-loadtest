package kr.kickon.api.domain.userGameGamble;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.migration.dto.ApiGamesDTO;
import kr.kickon.api.domain.userGameGamble.dto.GambleCountDTO;
import kr.kickon.api.domain.userPointDetail.UserPointDetailService;
import kr.kickon.api.domain.userPointEvent.UserPointEventService;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.*;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserGameGambleService implements BaseService<UserGameGamble> {
    private final UserGameGambleRepository userGameGambleRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    public static String[] ScheduledStatus = {"TBD", "NS"};
    public static String[] FinishedStatus = {"FT", "AET", "PEN"};
    private final UserPointEventService userPointEventService;
    private final UserPointDetailService userPointDetailService;

    @Override
    public UserGameGamble findById(String uuid) {
        BooleanExpression predicate = QUserGameGamble.userGameGamble.id.eq(uuid).and(QUserGameGamble.userGameGamble.status.eq(DataStatus.ACTIVATED));
        Optional<UserGameGamble> userGameGamble = userGameGambleRepository.findOne(predicate);
        return userGameGamble.orElse(null);
    }

    @Override
    public UserGameGamble findByPk(Long pk) {
        BooleanExpression predicate = QUserGameGamble.userGameGamble.pk.eq(pk).and(QUserGameGamble.userGameGamble.status.eq(DataStatus.ACTIVATED));
        Optional<UserGameGamble> userGameGamble = userGameGambleRepository.findOne(predicate);
        if(userGameGamble.isPresent()) return userGameGamble.get();
        throw new NotFoundException(ResponseCode.NOT_FOUND_USER_GAME_GAMBLE);
    }

    /**
     * 특정 게임에 대한 활성화된 유저 베팅 수를 predictedResult별로 그룹화하여 반환
     */
    public Map<PredictedResult, Long> findGambleCountByGamePk(Long gamePk) {
        List<GambleCountDTO> gambleCounts = queryFactory
                .select(Projections.constructor(GambleCountDTO.class,
                        QUserGameGamble.userGameGamble.predictedResult,
                        QUserGameGamble.userGameGamble.id.count()))
                .from(QUserGameGamble.userGameGamble)
                .where(QUserGameGamble.userGameGamble.game.pk.eq(gamePk)
                        .and(QUserGameGamble.userGameGamble.status.eq(DataStatus.ACTIVATED)))
                .groupBy(QUserGameGamble.userGameGamble.predictedResult)
                .fetch();
        // Map<predictedResult, count> 형태로 변환
        return gambleCounts.stream()
                .collect(Collectors.toMap(GambleCountDTO::getPredictedResult, GambleCountDTO::getCount));
    }

    public UserGameGamble findByUserAndGame(Long userPk, Long gamePk) {
        BooleanExpression predicate = QUserGameGamble.userGameGamble.user.pk.eq(userPk).and(QUserGameGamble.userGameGamble.status.eq(DataStatus.ACTIVATED).and(QUserGameGamble.userGameGamble.game.pk.eq(gamePk)));
        Optional<UserGameGamble> userGameGamble = userGameGambleRepository.findOne(predicate);
        return userGameGamble.orElse(null);
    }

    public List<UserGameGamble> findByGameApiId(Long gameApiId) {
        QUserGameGamble userGameGamble = QUserGameGamble.userGameGamble;
        return queryFactory
                .select(userGameGamble)
                .from(userGameGamble)
                .where(userGameGamble.game.apiId.eq(gameApiId)
                        .and(userGameGamble.status.eq(DataStatus.ACTIVATED)))
                .fetch();
    }

    public void updateGambleStatusByApiGamesDTO(List<UserGameGamble> userGameGambles, ApiGamesDTO apiGamesDTO, GameStatus gameStatus) {
        List<String> userPointEventIds = new ArrayList<>();
        List<String> userPointDetailIds = new ArrayList<>();
        for(UserGameGamble userGameGamble : userGameGambles) {
            // 경기 결과가 확정되지 않은 경우, 상태를 변경하지 않음
            if (gameStatus == GameStatus.PENDING || gameStatus == GameStatus.PROCEEDING ||
                    gameStatus == GameStatus.CANCELED || gameStatus == GameStatus.POSTPONED) {
                continue;
            }

            PredictedResult predictedResult = userGameGamble.getPredictedResult();
            GambleStatus gambleStatus;

            // 예측 결과와 실제 경기 결과 비교
            if ((predictedResult == PredictedResult.HOME && gameStatus == GameStatus.HOME) ||
                    (predictedResult == PredictedResult.AWAY && gameStatus == GameStatus.AWAY) ||
                    (predictedResult == PredictedResult.DRAW && gameStatus == GameStatus.DRAW)) {

                // 정확한 점수까지 예측했는지 확인
                if (userGameGamble.getPredictedHomeScore().equals(apiGamesDTO.getHomeScore()) &&
                        userGameGamble.getPredictedAwayScore().equals(apiGamesDTO.getAwayScore())) {
                    gambleStatus = GambleStatus.PERFECT; // 정확한 점수까지 맞춘 경우
                } else {
                    gambleStatus = GambleStatus.SUCCEED; // 승부 예측만 맞춘 경우
                }
            } else {
                gambleStatus = GambleStatus.FAILED; // 예측 실패
            }

            // 상태 업데이트
            userGameGamble.setGambleStatus(gambleStatus);
            // 상태에 따라 포인트를 적립
            int points = 0;
            if (userGameGamble.getGambleStatus() == GambleStatus.SUCCEED) {
                points = 1;
            } else if (userGameGamble.getGambleStatus() == GambleStatus.PERFECT) {
                points = 3;
            }

            if (points > 0) {
                // 포인트를 UserPointDetail에 추가
                String userPointEventId = "";
                String userPointDetailId = "";
                do {
                    userPointDetailId = uuidGenerator.generateUniqueUUID(userPointDetailService::findById);
                    // 이미 생성된 ID가 배열에 있는지 확인
                } while (userPointDetailIds.contains(userPointDetailId)); // 중복이 있을 경우 다시 생성
                do {
                    userPointEventId = uuidGenerator.generateUniqueUUID(userPointDetailService::findById);
                    // 이미 생성된 ID가 배열에 있는지 확인
                } while (userPointEventIds.contains(userPointEventId)); // 중복이 있을 경우 다시 생성
                userPointDetailIds.add(userPointDetailId);
                userPointEventIds.add(userPointEventId);
                UserPointEvent userPointEvent = UserPointEvent.builder()
                        .id(userPointEventId)
                        .point(points)
                        .pointStatus(PointStatus.SAVE)
                        .user(userGameGamble.getUser())
                        .category(PointCategory.GAMBLE)
                        .build();
                UserPointDetail userPointDetail = UserPointDetail.builder()
                        .id(userPointDetailId)
                        .pointStatus(PointStatus.SAVE)
                        .user(userGameGamble.getUser())
                        .point(points)
                        .userPointEvent(userPointEventService.save(userPointEvent))
                        .build();
                userPointDetailService.save(userPointDetail);
            }
        }
        userGameGambleRepository.saveAll(userGameGambles);
    }

    public void save(UserGameGamble userGameGamble) {
        userGameGambleRepository.save(userGameGamble);
    }
    public void saveAll(List<UserGameGamble> userGameGambles) { userGameGambleRepository.saveAll(userGameGambles); }

    public void delete(UserGameGamble userGameGamble) {
        userGameGambleRepository.delete(userGameGamble);
    }
}
