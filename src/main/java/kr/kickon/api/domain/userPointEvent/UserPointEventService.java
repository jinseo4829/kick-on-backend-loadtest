package kr.kickon.api.domain.userPointEvent;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.jsonwebtoken.lang.Collections;
import kr.kickon.api.domain.userPointEvent.dto.UserRankingDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPointEventService{
    private final UserPointEventRepository userPointEventRepository;
    private final JPAQueryFactory queryFactory;

    public UserPointEvent findByPk(Long pk) {
        BooleanExpression predicate = QNews.news.pk.eq(pk).and(QUserPointEvent.userPointEvent.status.eq(DataStatus.ACTIVATED));
        Optional<UserPointEvent> userPointEvent = userPointEventRepository.findOne(predicate);
        return userPointEvent.orElse(null);
    }

    public UserRankingDTO findUserRankingFromAll(LocalDateTime seasonStart, LocalDateTime seasonEnd, Long userPk){
        QUser user = QUser.user;
        QUserGameGamble userGameGamble = QUserGameGamble.userGameGamble;
        QGame game = QGame.game;
        QUserPointEvent userPointEvent = QUserPointEvent.userPointEvent;
        // 유효한 유저들의 포인트 합산 및 랭킹 계산
        List<Tuple> results = queryFactory
                .select(user.id, userPointEvent.point.sum(),
                        Expressions.numberTemplate(Integer.class, "RANK() OVER (ORDER BY SUM({0}) DESC)", userPointEvent.point))
                .from(userPointEvent)
                .where(userPointEvent.pointStatus.eq(PointStatus.SAVE)
                        .and(userPointEvent.createdAt.between(seasonStart, seasonEnd))
                        .and(userPointEvent.category.eq(PointCategory.valueOf("GAMBLE")))
                        .and(userPointEvent.status.eq(DataStatus.ACTIVATED)))
                .groupBy(user.pk)
                .having(user.pk.eq(userPk)) // 그룹화된 결과에서 현재 사용자만 필터링
                .orderBy(userPointEvent.point.sum().desc())
                .fetch();

        if(results.isEmpty()) return null;
        else{
            Tuple tuple = results.get(0);
            // DTO 변환
            return UserRankingDTO.builder()
                    .userId(tuple.get(user.id))
                    .totalPoints(tuple.get(userPointEvent.point.sum()))
                    .ranking(tuple.get(2, Integer.class))
                    .build();
        }
    }

    public UserRankingDTO findUserRankingByTeam(Long teamPk, LocalDateTime seasonStart, LocalDateTime seasonEnd, Long userPk) {
        QUser user = QUser.user;
        QUserGameGamble userGameGamble = QUserGameGamble.userGameGamble;
        QGame game = QGame.game;
        QUserPointEvent userPointEvent = QUserPointEvent.userPointEvent;
        // 서브쿼리: 해당 팀에서 'SUCCEED', 'FAILED', 'PERFECT' 예측을 한 활성화된 유저 목록
        List<GambleStatus> validResults = Arrays.asList(GambleStatus.SUCCEED,GambleStatus.FAILED,GambleStatus.PERFECT);
        List<Long> validUserPks = queryFactory
                .select(user.pk)
                .from(userGameGamble)
                .join(user).on(user.pk.eq(userGameGamble.user.pk))
                .join(game).on(userGameGamble.game.pk.eq(game.pk))
                .where(userGameGamble.supportingTeam.pk.eq(teamPk)
                        .and(user.status.eq(DataStatus.ACTIVATED))
                        .and(userGameGamble.status.eq(DataStatus.ACTIVATED))
                        .and(game.status.eq(DataStatus.ACTIVATED))
                        .and(userGameGamble.gambleStatus.in(validResults))
                        .and(game.startedAt.between(seasonStart, seasonEnd)))
                .fetch();

        // 유효한 유저들의 포인트 합산 및 랭킹 계산
        List<Tuple> results = queryFactory
                .select(user.id, userPointEvent.point.sum(),
                        Expressions.numberTemplate(Integer.class, "RANK() OVER (ORDER BY SUM({0}) DESC)", userPointEvent.point))
                .from(userPointEvent)
                .where(userPointEvent.user.pk.in(validUserPks)
                        .and(userPointEvent.pointStatus.eq(PointStatus.SAVE))
                        .and(userPointEvent.createdAt.between(seasonStart, seasonEnd))
                        .and(userPointEvent.category.eq(PointCategory.valueOf("GAMBLE")))
                        .and(userPointEvent.status.eq(DataStatus.ACTIVATED)))
                .groupBy(user.pk)
                .having(user.pk.eq(userPk)) // 그룹화된 결과에서 현재 사용자만 필터링
                .orderBy(userPointEvent.point.sum().desc())
                .fetch();

        if(results.isEmpty()) return null;
        else{
            Tuple tuple = results.get(0);
            // DTO 변환
            return UserRankingDTO.builder()
                    .userId(tuple.get(user.id))
                    .totalPoints(tuple.get(userPointEvent.point.sum()))
                    .ranking(tuple.get(2, Integer.class))
                    .build();
        }

    }

    public int getPointSumByUser(Long userPk, LocalDateTime from, LocalDateTime to) {
        QUserPointEvent pointEvent = QUserPointEvent.userPointEvent;

        var query = queryFactory
                .select(pointEvent.point.sum())
                .from(pointEvent)
                .where(pointEvent.user.pk.eq(userPk)
                        .and(pointEvent.pointStatus.eq(PointStatus.SAVE))
                        .and(pointEvent.category.eq(PointCategory.GAMBLE))
                        .and(pointEvent.status.eq(DataStatus.ACTIVATED)));

        if (from != null && to != null) {
            query.where(pointEvent.createdAt.between(from, to));
        }

        Integer sum = query.fetchOne();
        return sum != null ? sum : 0;
    }

    public UserPointEvent save(UserPointEvent userPointEvent) {
        return userPointEventRepository.save(userPointEvent);
    }
}