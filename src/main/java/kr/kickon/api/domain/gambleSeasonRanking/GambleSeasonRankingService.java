package kr.kickon.api.domain.gambleSeasonRanking;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.actualSeasonRanking.dto.GetActualSeasonRankingDTO;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GambleSeasonRankingService implements BaseService<GambleSeasonRanking> {
    private final GambleSeasonRankingRepository gambleSeasonRankingRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public GambleSeasonRanking findById(String uuid) {
        BooleanExpression predicate = QGambleSeasonRanking.gambleSeasonRanking.id.eq(uuid).and(QGambleSeasonRanking.gambleSeasonRanking.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeasonRanking> gambleSeasonRanking = gambleSeasonRankingRepository.findOne(predicate);
        return gambleSeasonRanking.orElse(null);
    }

    @Override
    public GambleSeasonRanking findByPk(Long pk) {
        BooleanExpression predicate = QGambleSeasonRanking.gambleSeasonRanking.pk.eq(pk).and(QGambleSeasonRanking.gambleSeasonRanking.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeasonRanking> gambleSeasonRanking = gambleSeasonRankingRepository.findOne(predicate);
        return gambleSeasonRanking.orElse(null);
    }

    public List<GetGambleSeasonRankingDTO> findRecentSeasonRankingByLeague(Long gambleSeasonPk) {
        QGambleSeasonRanking gambleSeasonRanking = QGambleSeasonRanking.gambleSeasonRanking;
        QTeam team = QTeam.team;

        return queryFactory
                .select(Projections.constructor(
                        GetGambleSeasonRankingDTO.class,
                        gambleSeasonRanking.rankOrder,
                        team.logoUrl,
                        team.nameKr,
                        gambleSeasonRanking.gameNum,
                        gambleSeasonRanking.points
                ))
                .from(gambleSeasonRanking)
                .join(gambleSeasonRanking.team, team)
                .where(gambleSeasonRanking.status.eq(DataStatus.ACTIVATED)
                        .and(gambleSeasonRanking.gambleSeason.pk.eq(gambleSeasonPk).and(gambleSeasonRanking.status.eq(DataStatus.ACTIVATED))))
                .orderBy(gambleSeasonRanking.rankOrder.asc())
                .fetch();
    }
}
