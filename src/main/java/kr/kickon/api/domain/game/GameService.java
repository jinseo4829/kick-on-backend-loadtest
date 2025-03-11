package kr.kickon.api.domain.game;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.GameStatus;
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
        if(game.isPresent()) return game.get();
        throw new NotFoundException(ResponseCode.NOT_FOUND_GAME);
    }

    public Game findByApiId(Long apiId){
        BooleanExpression predicate = QGame.game.apiId.eq(apiId).and(QGame.game.status.eq(DataStatus.ACTIVATED));
        Optional<Game> game = gameRepository.findOne(predicate);
        if(game.isPresent()) return game.get();
        throw new NotFoundException(ResponseCode.NOT_FOUND_GAME);
    }

    public void save(Game game) {
        gameRepository.save(game);
    }

    public List<Game> findByActualSeason(Long actualSeasonPk, String gameStatus){
        JPAQuery<Game> query = queryFactory.selectFrom(QGame.game)
                .where(QGame.game.status.eq(DataStatus.ACTIVATED).and(QGame.game.actualSeason.pk.eq(actualSeasonPk)));

        if (gameStatus.equals("finished")) {
            query.where(QGame.game.gameStatus.in(GameStatus.PROCEEDING, GameStatus.CANCELED, GameStatus.HOME,
                    GameStatus.AWAY, GameStatus.DRAW))
                    .orderBy(QGame.game.startedAt.desc());
        } else {
            query.where(QGame.game.gameStatus.in(GameStatus.POSTPONED, GameStatus.PENDING))
                    .orderBy(QGame.game.startedAt.asc());
        }

        return query.limit(2).fetch();
    }

    public List<Game> findByActualSeasonByFavoriteTeam(Long actualSeasonPk, String gameStatus, Long favoriteTeamPk) {
        JPAQuery<Game> query = queryFactory.selectFrom(QGame.game)
                .where(QGame.game.status.eq(DataStatus.ACTIVATED)
                        .and(QGame.game.actualSeason.pk.eq(actualSeasonPk))
                        .and(QGame.game.homeTeam.pk.eq(favoriteTeamPk)
                                .or(QGame.game.awayTeam.pk.eq(favoriteTeamPk))));

        if (gameStatus.equals("finished")) {
            query.where(QGame.game.gameStatus.in(GameStatus.PROCEEDING, GameStatus.CANCELED, GameStatus.HOME,
                            GameStatus.AWAY, GameStatus.DRAW))
                    .orderBy(QGame.game.startedAt.desc());
        } else {
            query.where(QGame.game.gameStatus.in(GameStatus.POSTPONED, GameStatus.PENDING))
                    .orderBy(QGame.game.startedAt.asc());
        }

        return query.limit(2).fetch();
    }

}
