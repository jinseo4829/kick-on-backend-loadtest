package kr.kickon.api.domain.actualSeasonRanking;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.actualSeasonRanking.dto.GetActualSeasonRankingDTO;
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
public class ActualSeasonRankingService implements BaseService<ActualSeasonRanking> {
    private final ActualSeasonRankingRepository actualSeasonRankingRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public ActualSeasonRanking findById(String uuid) {
        BooleanExpression predicate = QActualSeasonRanking.actualSeasonRanking.id.eq(uuid).and(QActualSeasonRanking.actualSeasonRanking.status.eq(DataStatus.ACTIVATED));
        Optional<ActualSeasonRanking> actualSeasonRanking = actualSeasonRankingRepository.findOne(predicate);
        return actualSeasonRanking.orElse(null);
    }

    @Override
    public ActualSeasonRanking findByPk(Long pk) {
        BooleanExpression predicate = QActualSeasonRanking.actualSeasonRanking.pk.eq(pk).and(QActualSeasonRanking.actualSeasonRanking.status.eq(DataStatus.ACTIVATED));
        Optional<ActualSeasonRanking> actualSeasonRanking = actualSeasonRankingRepository.findOne(predicate);
        return actualSeasonRanking.orElse(null);
    }

    public ActualSeasonRanking findByActualSeasonAndTeam(Long actualSeason, Long team) {
        BooleanExpression predicate = QActualSeasonRanking.actualSeasonRanking.actualSeason.pk.eq(actualSeason).and(QActualSeasonRanking.actualSeasonRanking.status.eq(DataStatus.ACTIVATED).and(QActualSeasonRanking.actualSeasonRanking.team.pk.eq(team)));
        Optional<ActualSeasonRanking> actualSeasonRanking = actualSeasonRankingRepository.findOne(predicate);
        return actualSeasonRanking.orElse(null);
    }

    public List<GetActualSeasonRankingDTO> findRecentSeasonRankingByLeague(Long actualSeasonPk) {
        QActualSeasonRanking actualSeasonRanking = QActualSeasonRanking.actualSeasonRanking;
        QTeam team = QTeam.team;

        return queryFactory
                .select(Projections.constructor(
                        GetActualSeasonRankingDTO.class,
                        actualSeasonRanking.rankOrder,
                        team.logoUrl,
                        team.nameKr,
                        actualSeasonRanking.gameNum,
                        actualSeasonRanking.points,
                        actualSeasonRanking.wonScores
                ))
                .from(actualSeasonRanking)
                .join(actualSeasonRanking.team, team)
                .where(actualSeasonRanking.status.eq(DataStatus.ACTIVATED).and(actualSeasonRanking.actualSeason.pk.eq(actualSeasonPk)))
                .orderBy(actualSeasonRanking.rankOrder.asc())
                .fetch();
    }

    public void save(ActualSeasonRanking actualSeasonRanking){
        actualSeasonRankingRepository.save(actualSeasonRanking);
    }
}
