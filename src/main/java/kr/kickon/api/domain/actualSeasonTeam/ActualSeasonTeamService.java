package kr.kickon.api.domain.actualSeasonTeam;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.entities.QActualSeason;
import kr.kickon.api.global.common.entities.QActualSeasonTeam;
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
public class ActualSeasonTeamService implements BaseService<ActualSeasonTeam> {
    private final ActualSeasonTeamRepository actualSeasonTeamRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
//    public List<User> findUserByEmail(String email){
//        // JPAQueryFactory
//        return queryFactory.selectFrom(QUser.user)
//                .where(QUser.user.email.eq(email))
//                .fetch();
//    }
    @Override
    public ActualSeasonTeam findById(String uuid) {
        BooleanExpression predicate = QActualSeasonTeam.actualSeasonTeam.id.eq(uuid).and(QActualSeasonTeam.actualSeasonTeam.status.eq(DataStatus.ACTIVATED));
        Optional<ActualSeasonTeam> actualSeasonTeam = actualSeasonTeamRepository.findOne(predicate);
        return actualSeasonTeam.orElse(null);
    }

    @Override
    public ActualSeasonTeam findByPk(Long pk) {
        BooleanExpression predicate = QActualSeasonTeam.actualSeasonTeam.pk.eq(pk).and(QActualSeasonTeam.actualSeasonTeam.status.eq(DataStatus.ACTIVATED));
        Optional<ActualSeasonTeam> actualSeasonTeam = actualSeasonTeamRepository.findOne(predicate);
        if (actualSeasonTeam.isPresent()) return actualSeasonTeam.get();
        throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON_TEAM);
    }

    public ActualSeasonTeam findByActualSeason(ActualSeason actualSeason,Long teamPk) {
        BooleanExpression predicate = QActualSeasonTeam.actualSeasonTeam.actualSeason.pk.eq(actualSeason.getPk()).and(QActualSeasonTeam.actualSeasonTeam.status.eq(DataStatus.ACTIVATED).and(QActualSeasonTeam.actualSeasonTeam.team.pk.eq(teamPk)));
        Optional<ActualSeasonTeam> actualSeasonTeam = actualSeasonTeamRepository.findOne(predicate);
        if (actualSeasonTeam.isPresent()) return actualSeasonTeam.get();
        throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON_TEAM);
    }

    public ActualSeasonTeam save(ActualSeasonTeam actualSeasonTeam) {
        return actualSeasonTeamRepository.save(actualSeasonTeam);
    }
}
