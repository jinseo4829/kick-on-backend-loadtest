package kr.kickon.api.domain.actualSeason;

import com.querydsl.core.types.dsl.BooleanExpression;
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

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActualSeasonService implements BaseService<ActualSeason> {
    private final ActualSeasonRepository actualSeasonRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
//    public List<User> findUserByEmail(String email){
//        // JPAQueryFactory
//        return queryFactory.selectFrom(QUser.user)
//                .where(QUser.user.email.eq(email))
//                .fetch();
//    }
    @Override
    public ActualSeason findById(String uuid) {
        BooleanExpression predicate = QActualSeason.actualSeason.id.eq(uuid).and(QActualSeason.actualSeason.status.eq(DataStatus.ACTIVATED));
        Optional<ActualSeason> actualSeason = actualSeasonRepository.findOne(predicate);
        if(actualSeason.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_LEAGUE);
        return actualSeason.get();
    }

    @Override
    public ActualSeason findByPk(Long pk) {
        BooleanExpression predicate = QActualSeason.actualSeason.pk.eq(pk).and(QActualSeason.actualSeason.status.eq(DataStatus.ACTIVATED));
        Optional<ActualSeason> actualSeason = actualSeasonRepository.findOne(predicate);
        if(actualSeason.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_LEAGUE);
        return actualSeason.get();
    }



    public ActualSeason findRecentByLeaguePk(Long pk){
        BooleanExpression predicate = QActualSeason.actualSeason.league.pk.eq(pk).and(QLeague.league.status.eq(DataStatus.ACTIVATED));
        Optional<ActualSeason> actualSeason = actualSeasonRepository.findOne(predicate);
        if(actualSeason.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_LEAGUE_BY_LEAGUE);
        return actualSeason.get();
    }
}
