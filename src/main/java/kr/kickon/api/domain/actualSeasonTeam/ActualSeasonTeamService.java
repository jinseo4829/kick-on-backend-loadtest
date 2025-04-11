package kr.kickon.api.domain.actualSeasonTeam;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.entities.QActualSeasonTeam;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActualSeasonTeamService implements BaseService<ActualSeasonTeam> {
    private final ActualSeasonTeamRepository actualSeasonTeamRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
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
        return actualSeasonTeam.orElse(null);
    }

    public ActualSeasonTeam findByActualSeasonTeam(ActualSeason actualSeason,Long teamPk) {
        BooleanExpression predicate = QActualSeasonTeam.actualSeasonTeam.actualSeason.pk.eq(actualSeason.getPk()).and(QActualSeasonTeam.actualSeasonTeam.status.eq(DataStatus.ACTIVATED).and(QActualSeasonTeam.actualSeasonTeam.team.pk.eq(teamPk)));
        Optional<ActualSeasonTeam> actualSeasonTeam = actualSeasonTeamRepository.findOne(predicate);
        return actualSeasonTeam.orElse(null);
    }

    public List<ActualSeasonTeam> findByActualSeason(Long actualSeasonPk, String keyword){
        QActualSeasonTeam actualSeasonTeam = QActualSeasonTeam.actualSeasonTeam;

        JPAQuery<ActualSeasonTeam> query = queryFactory
                .selectFrom(actualSeasonTeam)
                .where(
                        actualSeasonTeam.actualSeason.pk.eq(actualSeasonPk),
                        actualSeasonTeam.status.eq(DataStatus.ACTIVATED),
                        actualSeasonTeam.team.status.eq(DataStatus.ACTIVATED),
                        actualSeasonTeam.actualSeason.operatingStatus.eq(OperatingStatus.PROCEEDING)
                )
                .orderBy(actualSeasonTeam.createdAt.desc());
        if(!keyword.isEmpty()) query.where(actualSeasonTeam.team.nameKr.containsIgnoreCase(keyword)
                .or(actualSeasonTeam.team.nameEn.containsIgnoreCase(keyword)));
//                .fetch();
        return query.fetch();
    }

    public ActualSeasonTeam findLatestByTeam(Long teamPk) {
        QActualSeasonTeam actualSeasonTeam = QActualSeasonTeam.actualSeasonTeam;

        return queryFactory
                .selectFrom(actualSeasonTeam)
                .where(
                        actualSeasonTeam.team.pk.eq(teamPk),
                        actualSeasonTeam.status.eq(DataStatus.ACTIVATED),
                        actualSeasonTeam.team.status.eq(DataStatus.ACTIVATED),
                        actualSeasonTeam.actualSeason.operatingStatus.eq(OperatingStatus.PROCEEDING),
                        actualSeasonTeam.actualSeason.status.eq(DataStatus.ACTIVATED),
                        actualSeasonTeam.actualSeason.league.status.eq(DataStatus.ACTIVATED)

                )
                .orderBy(actualSeasonTeam.createdAt.desc()) // 최신 순 정렬
                .fetchFirst(); // 가장 최신 1개만 조회
    }

    public ActualSeasonTeam save(ActualSeasonTeam actualSeasonTeam) {
        return actualSeasonTeamRepository.save(actualSeasonTeam);
    }
}
