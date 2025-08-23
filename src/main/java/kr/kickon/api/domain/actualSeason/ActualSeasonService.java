package kr.kickon.api.domain.actualSeason;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
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
public class ActualSeasonService {
    private final ActualSeasonRepository actualSeasonRepository;
    private final JPAQueryFactory queryFactory;
//    public List<User> findUserByEmail(String email){
//        // JPAQueryFactory
//        return queryFactory.selectFrom(QUser.user)
//                .where(QUser.user.email.eq(email))
//                .fetch();
//    }

    public ActualSeason findByPk(Long pk) {
        BooleanExpression predicate = QActualSeason.actualSeason.pk.eq(pk).and(QActualSeason.actualSeason.status.eq(DataStatus.ACTIVATED));
        Optional<ActualSeason> actualSeason = actualSeasonRepository.findOne(predicate);
        return actualSeason.orElse(null);
    }

    public ActualSeason findByYearAndLeague(Integer year, Long league){
        BooleanExpression predicate = QActualSeason.actualSeason.year.eq(year)
                .and(QActualSeason.actualSeason.status.eq(DataStatus.ACTIVATED))
                .and(QActualSeason.actualSeason.league.pk.eq(league).and(QActualSeason.actualSeason.league.status.eq(DataStatus.ACTIVATED)));
        Optional<ActualSeason> actualSeason = actualSeasonRepository.findOne(predicate);
        return actualSeason.orElse(null);
    }

    public ActualSeason findRecentByLeaguePk(Long pk){
        JPAQuery<ActualSeason> query = queryFactory.selectFrom(QActualSeason.actualSeason)
                .where(QActualSeason.actualSeason.league.pk.eq(pk).and(QLeague.league.status.eq(DataStatus.ACTIVATED)))
                .orderBy(QActualSeason.actualSeason.year.desc());
        List<ActualSeason> actualSeason = query.fetch();
        return actualSeason.isEmpty() ? null : actualSeason.get(0);
    }

    public void save(ActualSeason actualSeason) {
        actualSeasonRepository.save(actualSeason);
    }
}
