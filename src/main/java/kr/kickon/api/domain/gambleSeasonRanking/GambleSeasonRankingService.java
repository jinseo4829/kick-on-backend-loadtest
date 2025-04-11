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

import java.util.Comparator;
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

    public List<GambleSeasonRanking> findRecentSeasonRankingByLeague(Long gambleSeasonPk) {
        QGambleSeasonRanking gambleSeasonRanking = QGambleSeasonRanking.gambleSeasonRanking;
        QTeam team = QTeam.team;

        return queryFactory
                .selectFrom(gambleSeasonRanking)
                .join(gambleSeasonRanking.team, team)
                .where(gambleSeasonRanking.status.eq(DataStatus.ACTIVATED)
                        .and(gambleSeasonRanking.gambleSeason.pk.eq(gambleSeasonPk).and(gambleSeasonRanking.status.eq(DataStatus.ACTIVATED))))
                .orderBy(gambleSeasonRanking.rankOrder.asc())
                .fetch();
    }

    public List<GambleSeasonRanking> findRecentSeasonRankingByGambleSeason(Long gambleSeasonPk) {
        QGambleSeasonRanking gambleSeasonRanking = QGambleSeasonRanking.gambleSeasonRanking;

        return queryFactory
                .select(gambleSeasonRanking)
                .from(gambleSeasonRanking)
                .where(gambleSeasonRanking.status.eq(DataStatus.ACTIVATED)
                        .and(gambleSeasonRanking.gambleSeason.pk.eq(gambleSeasonPk).and(gambleSeasonRanking.status.eq(DataStatus.ACTIVATED))))
                .orderBy(gambleSeasonRanking.rankOrder.asc())
                .fetch();
    }

    public GambleSeasonRanking findByTeamPk(Long teamPk) {
        BooleanExpression predicate = QGambleSeasonRanking.gambleSeasonRanking.team.pk.eq(teamPk).and(QGambleSeasonRanking.gambleSeasonRanking.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeasonRanking> gambleSeasonRanking = gambleSeasonRankingRepository.findOne(predicate);
        return gambleSeasonRanking.orElse(null);
    }

    public void recalculateRanking(List<GambleSeasonRanking> ranking) {
        // 1. 포인트 기준 내림차순, 경기 수 기준 오름차순 정렬
        ranking.sort(Comparator
                .comparingInt(GambleSeasonRanking::getPoints).reversed()
                .thenComparingInt(GambleSeasonRanking::getGameNum));

        int currentRank = 1;

        for (GambleSeasonRanking current : ranking) {
            current.setRankOrder(currentRank);
            currentRank++; // 다음 순위 증가
        }

        // 변경된 랭킹을 저장
        gambleSeasonRankingRepository.saveAll(ranking);
    }

    public void save(GambleSeasonRanking gambleSeasonRanking) {
        gambleSeasonRankingRepository.save(gambleSeasonRanking);
    }
}
