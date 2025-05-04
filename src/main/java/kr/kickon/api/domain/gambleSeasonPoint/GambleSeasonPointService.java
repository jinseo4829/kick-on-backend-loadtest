package kr.kickon.api.domain.gambleSeasonPoint;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.GambleSeasonPoint;
import kr.kickon.api.global.common.entities.GambleSeasonRanking;
import kr.kickon.api.global.common.entities.QGambleSeasonPoint;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GambleSeasonPointService implements BaseService<GambleSeasonPoint> {
    private final GambleSeasonPointRepository gambleSeasonPointRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public GambleSeasonPoint findById(String uuid) {
        BooleanExpression predicate = QGambleSeasonPoint.gambleSeasonPoint.id.eq(uuid).and(QGambleSeasonPoint.gambleSeasonPoint.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeasonPoint> gambleSeasonPoint = gambleSeasonPointRepository.findOne(predicate);
        return gambleSeasonPoint.orElse(null);
    }

    @Override
    public GambleSeasonPoint findByPk(Long pk) {
        BooleanExpression predicate = QGambleSeasonPoint.gambleSeasonPoint.pk.eq(pk).and(QGambleSeasonPoint.gambleSeasonPoint.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeasonPoint> gambleSeasonPoint = gambleSeasonPointRepository.findOne(predicate);
        return gambleSeasonPoint.orElse(null);
    }

    public Integer findTotalPointByGambleSeasonAndTeam(Long gambleSeasonPk, Long teamPk) {
        QGambleSeasonPoint gambleSeasonPoint = QGambleSeasonPoint.gambleSeasonPoint;

        Integer totalPoint = queryFactory
                .select(gambleSeasonPoint.averagePoints.sum())
                .from(gambleSeasonPoint)
                .where(
                        gambleSeasonPoint.gambleSeason.pk.eq(gambleSeasonPk)
                                .and(gambleSeasonPoint.team.pk.eq(teamPk))
                                .and(gambleSeasonPoint.status.eq(DataStatus.ACTIVATED))
                )
                .fetchOne();

        return totalPoint != null ? totalPoint : 0; // null 처리 중요
    }

    public GambleSeasonPoint findByTeamPkAndGamePk(Long teamPk, Long gamePk) {
        BooleanExpression predicate = QGambleSeasonPoint.gambleSeasonPoint.team.pk.eq(teamPk).and(QGambleSeasonPoint.gambleSeasonPoint.status.eq(DataStatus.ACTIVATED).and(QGambleSeasonPoint.gambleSeasonPoint.game.pk.eq(gamePk)));
        Optional<GambleSeasonPoint> gambleSeasonPoint = gambleSeasonPointRepository.findOne(predicate);
        return gambleSeasonPoint.orElse(null);
    }
    
    public void save(GambleSeasonPoint gambleSeasonPoint) {
        gambleSeasonPointRepository.save(gambleSeasonPoint);
    }
}
