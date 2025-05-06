package kr.kickon.api.domain.game;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.migration.dto.ApiGamesDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.GameStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class GameService implements BaseService<Game> {
    private final GameRepository gameRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    public static String[] ScheduledStatus = {"TBD", "NS"};
    public static String[] FinishedStatus = {"FT", "AET", "PEN"};

    @Override
    public Game findById(String uuid) {
        BooleanExpression predicate = QGame.game.id.eq(uuid).and(QGame.game.status.eq(DataStatus.ACTIVATED));
        Optional<Game> game = gameRepository.findOne(predicate);
        return game.orElse(null);
    }

    @Override
    public Game findByPk(Long pk) {
        BooleanExpression predicate = QGame.game.pk.eq(pk).and(QGame.game.status.eq(DataStatus.ACTIVATED));
        Optional<Game> game = gameRepository.findOne(predicate);
        return game.orElse(null);
    }

    public Game findByApiId(Long apiId){
        BooleanExpression predicate = QGame.game.apiId.eq(apiId).and(QGame.game.status.eq(DataStatus.ACTIVATED));
        Optional<Game> game = gameRepository.findOne(predicate);
        if(game.isPresent()) return game.get();
        throw new NotFoundException(ResponseCode.NOT_FOUND_GAME);
    }

    public Game save(Game game) {
        return gameRepository.save(game);
    }

    public List<Game> findByActualSeason(Long actualSeasonPk, String gameStatus){
        JPAQuery<Game> query = queryFactory.selectFrom(QGame.game)
                .where(QGame.game.status.eq(DataStatus.ACTIVATED).and(QGame.game.actualSeason.pk.eq(actualSeasonPk)));

        LocalDateTime now = LocalDateTime.now();
        if (gameStatus.equals("finished")) {
            query.where(QGame.game.gameStatus.in(GameStatus.PROCEEDING, GameStatus.CANCELED, GameStatus.HOME,
                            GameStatus.AWAY, GameStatus.DRAW, GameStatus.PENDING).and(QGame.game.startedAt.lt(now.minusHours(2))))
                    .orderBy(QGame.game.startedAt.desc());
        } else {
            query.where(QGame.game.gameStatus.in(GameStatus.POSTPONED, GameStatus.PENDING).and(QGame.game.startedAt.goe(now.plusHours(2))))
                    .orderBy(QGame.game.startedAt.asc());
        }

        return query.orderBy(QGame.game.startedAt.asc()).limit(2).fetch();
    }

    public List<Game> findByActualSeasonByFavoriteTeam(Long actualSeasonPk, String gameStatus, Long favoriteTeamPk) {
        LocalDateTime now = LocalDateTime.now();
        JPAQuery<Game> query = queryFactory.selectFrom(QGame.game)
                .where(QGame.game.status.eq(DataStatus.ACTIVATED)
                        .and(QGame.game.actualSeason.pk.eq(actualSeasonPk))
                        .and(QGame.game.homeTeam.pk.eq(favoriteTeamPk)
                                .or(QGame.game.awayTeam.pk.eq(favoriteTeamPk))));

        if (gameStatus.equals("finished")) {
            query.where(QGame.game.gameStatus.in(GameStatus.PROCEEDING, GameStatus.CANCELED, GameStatus.HOME,
                            GameStatus.AWAY, GameStatus.DRAW, GameStatus.PENDING).and(QGame.game.startedAt.lt(now.minusHours(2))))
                    .orderBy(QGame.game.startedAt.desc());
        } else {
            query.where(QGame.game.gameStatus.in(GameStatus.POSTPONED, GameStatus.PENDING).and(QGame.game.startedAt.goe(now.plusHours(2))))
                    .orderBy(QGame.game.startedAt.asc());
        }

        return query.limit(2).fetch();
    }

    public List<Game> findByToday() {
        // QGame 객체 생성
        QGame game = QGame.game;

        // 현재 시간과 4시간 전 시간 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past24Hours = now.minusHours(4);

        // QueryDSL을 사용하여 24시간 이내에 시작한 게임 중 종료된 게임을 조회
        return queryFactory
                .selectFrom(game)
                .where(
                        game.startedAt.between(past24Hours, now),  // 24시간 이내 시작한 게임
                        game.gameStatus.in(GameStatus.PENDING, GameStatus.POSTPONED,GameStatus.PROCEEDING),
                        game.status.eq(DataStatus.ACTIVATED)
                )
                .orderBy(game.startedAt.desc()) // 최신순 정렬
                .fetch();
    }

    public static @NotNull GameStatus getGameStatus(ApiGamesDTO apiData, List<String> scheduledStatus, List<String> finishedStatus) {
        GameStatus gameStatus;
        if(scheduledStatus.contains(apiData.getStatus())){
            // 시작 안한 경기
            gameStatus = GameStatus.PENDING;
        }

        else if(finishedStatus.contains(apiData.getStatus())){
            // 끝난 경기
            if(apiData.getStatus().equals("PEN")){
                // 승부차기인지 체크
                gameStatus = apiData.getHomePenaltyScore() > apiData.getAwayPenaltyScore() ? GameStatus.HOME : GameStatus.AWAY;
            } else {
                // 일반적으로 경기 마무리 된 경우
                gameStatus = apiData.getHomeScore().equals(apiData.getAwayScore()) ? GameStatus.DRAW : apiData.getHomeScore() > apiData.getAwayScore() ? GameStatus.HOME : GameStatus.AWAY;
            }
        } else if(apiData.getStatus().equals("PST")){
            // 연기된 경기
            gameStatus = GameStatus.POSTPONED;
        } else if(apiData.getStatus().equals("CANC") || apiData.getStatus().equals("ABD")){
            // 취소된 경기
            gameStatus = GameStatus.CANCELED;
        } else {
            // 진행중인 경기
            gameStatus = GameStatus.PROCEEDING;
        }
        return gameStatus;
    }
}
