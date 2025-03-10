package kr.kickon.api.domain.gambleSeason;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.eventBoard.dto.GetEventBoardDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.EventBoard;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.QEventBoard;
import kr.kickon.api.global.common.entities.QGambleSeason;
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
public class GambleSeasonService implements BaseService<GambleSeason> {
    private final GambleSeasonRepository gambleSeasonRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public GambleSeason findById(String uuid) {
        BooleanExpression predicate = QGambleSeason.gambleSeason.id.eq(uuid).and(QGambleSeason.gambleSeason.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeason> gambleSeason = gambleSeasonRepository.findOne(predicate);
        return gambleSeason.orElse(null);
    }

    @Override
    public GambleSeason findByPk(Long pk) {
        BooleanExpression predicate = QGambleSeason.gambleSeason.pk.eq(pk).and(QGambleSeason.gambleSeason.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeason> gambleSeason = gambleSeasonRepository.findOne(predicate);
        if(gambleSeason.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_GAMBLE_SEASON);
        return gambleSeason.get();
    }

    public GambleSeason findRecentOperatingSeasonByLeaguePk(Long leaguePk) {
        List<GambleSeason> gambleSeason = queryFactory.selectFrom(QGambleSeason.gambleSeason)
                .where(QGambleSeason.gambleSeason.actualSeason.league.pk.eq(leaguePk)
                        .and(QGambleSeason.gambleSeason.status.eq(DataStatus.ACTIVATED)
                        .and(QGambleSeason.gambleSeason.operatingStatus.eq(OperatingStatus.PROCEEDING))
                        ))
                .orderBy(QGambleSeason.gambleSeason.createdAt.desc())
                .limit(1)
                .fetch();

        if(gambleSeason.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_GAMBLE_SEASON);
        return gambleSeason.get(0);
    }
}
