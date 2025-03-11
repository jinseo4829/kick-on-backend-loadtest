package kr.kickon.api.domain.userGameGamble;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.userGameGamble.dto.GambleCountDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.Game;
import kr.kickon.api.global.common.entities.QGame;
import kr.kickon.api.global.common.entities.QUserGameGamble;
import kr.kickon.api.global.common.entities.UserGameGamble;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.GambleStatus;
import kr.kickon.api.global.common.enums.PredictedResult;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        if(userGameGamble.isPresent()) return userGameGamble.get();
        throw new NotFoundException(ResponseCode.NOT_FOUND_USER_GAME_GAMBLE);
    }

    public void save(UserGameGamble userGameGamble) {
        userGameGambleRepository.save(userGameGamble);
    }
}
