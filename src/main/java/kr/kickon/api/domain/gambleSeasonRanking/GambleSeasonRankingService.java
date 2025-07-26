package kr.kickon.api.domain.gambleSeasonRanking;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.actualSeasonRanking.dto.GetActualSeasonRankingDTO;
import kr.kickon.api.domain.gambleSeasonPoint.GambleSeasonPointService;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.GameStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GambleSeasonRankingService implements BaseService<GambleSeasonRanking> {
    private final GambleSeasonRankingRepository gambleSeasonRankingRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final GambleSeasonPointService gambleSeasonPointService;

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
            Integer gambleSeasonPoints = gambleSeasonPointService.findTotalPointByGambleSeasonAndTeam(current.getGambleSeason().getPk(), current.getTeam().getPk());
            current.setRankOrder(currentRank);
            current.setPoints(gambleSeasonPoints);
            currentRank++; // 다음 순위 증가
        }

        // 변경된 랭킹을 저장
        gambleSeasonRankingRepository.saveAll(ranking);
    }

    public void save(GambleSeasonRanking gambleSeasonRanking) {
        gambleSeasonRankingRepository.save(gambleSeasonRanking);
    }

    public void updateGameNumOnlyByActualSeason(Long actualSeason) {
        QGame game = QGame.game;
        QGambleSeasonRanking ranking = QGambleSeasonRanking.gambleSeasonRanking;

        // 1. 랭킹 먼저 조회 (실제 갬블 시즌으로)
        List<GambleSeasonRanking> rankings = queryFactory
                .selectFrom(ranking)
                .where(ranking.gambleSeason.actualSeason.pk.eq(actualSeason)
                        .and(ranking.status.eq(DataStatus.ACTIVATED)))
                .fetch();

        for (GambleSeasonRanking r : rankings) {
            // 해당 팀의 해당 시즌 게임 수를 가져온다
            Long gameCount = queryFactory
                    .select(game.count())
                    .from(game)
                    .where(game.status.eq(DataStatus.ACTIVATED)
                            .and(game.gameStatus.in(GameStatus.HOME, GameStatus.AWAY, GameStatus.DRAW))
                            .and(game.actualSeason.pk.eq(actualSeason))
                            .and(game.startedAt.goe(r.getGambleSeason().getStartedAt()))
                            .and(game.startedAt.loe(r.getGambleSeason().getFinishedAt()))
                            .and(game.homeTeam.pk.eq(r.getTeam().getPk())
                                    .or(game.awayTeam.pk.eq(r.getTeam().getPk())))
                    )
                    .fetchOne();

            r.setGameNum(gameCount != null ? gameCount.intValue() : 0);
        }

        gambleSeasonRankingRepository.saveAll(rankings);
    }

    @Transactional
    public List<GetGambleSeasonRankingDTO> getRankingDtoBySeasonPk(Long seasonPk) {

        // 랭킹 엔티티를 순위 기준으로 조회
        List<GambleSeasonRanking> rankings =
            findRecentSeasonRankingByGambleSeason(seasonPk);

        // DTO 변환
        return rankings.stream()
            .map(r -> {
                Double points = Optional.ofNullable(r.getPoints())
                    .map(Integer::doubleValue)
                    .orElseGet(() ->
                        gambleSeasonPointService
                            .findTotalPointByGambleSeasonAndTeam(
                                r.getGambleSeason().getPk(),
                                r.getTeam().getPk())
                            .doubleValue()
                    );

                return GetGambleSeasonRankingDTO.builder()
                    .rankOrder(r.getRankOrder())
                    .teamLogoUrl(r.getTeam().getLogoUrl())
                    .teamName(r.getTeam().getNameKr())
                    .gameNum(r.getGameNum())
                    .points(points)
                    .build();
            })
            .toList();
    }

    public GambleSeasonRanking findByGambleSeasonAndTeam(Long gambleSeasonPk, Long teamPk) {
        BooleanExpression predicate = QGambleSeasonRanking.gambleSeasonRanking.gambleSeason.pk.eq(gambleSeasonPk).and(QGambleSeasonRanking.gambleSeasonRanking.status.eq(DataStatus.ACTIVATED).and(QGambleSeasonRanking.gambleSeasonRanking.team.pk.eq(teamPk)));
        Optional<GambleSeasonRanking> gambleSeasonRanking = gambleSeasonRankingRepository.findOne(predicate);
        return gambleSeasonRanking.orElse(null);
    }

//region 승부 예측 시즌 변경 시 랭킹 갱신
    /**
     * 기존 랭킹을 비활성화하고, 새로운 GambleSeason에 대한 랭킹 엔티티를 생성한다.
     */
    @Transactional
    public void updateGambleSeasonRanking(Team team, GambleSeason newSeason) {
        // 기존 랭킹 비활성화
        GambleSeasonRanking oldRanking = findByTeamPk(team.getPk());
        if (oldRanking != null) {
            oldRanking.setStatus(DataStatus.DEACTIVATED);
            gambleSeasonRankingRepository.save(oldRanking);
        }

        // 새 랭킹 생성
        GambleSeasonRanking newRanking = GambleSeasonRanking.builder()
            .id(UUID.randomUUID().toString())
            .gambleSeason(newSeason)
            .team(team)
            .rankOrder(0)
            .gameNum(0)
            .points(0)
            .status(DataStatus.ACTIVATED)
            .build();

        gambleSeasonRankingRepository.save(newRanking);
    }
//endregion
}
