package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGame is a Querydsl query type for Game
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGame extends EntityPathBase<Game> {

    private static final long serialVersionUID = -389710319L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGame game = new QGame("game");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final QActualSeason actualSeason;

    public final NumberPath<Integer> awayScore = createNumber("awayScore", Integer.class);

    public final QTeam awayTeam;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath etc = _super.etc;

    public final DateTimePath<java.time.LocalDateTime> finishedAt = createDateTime("finishedAt", java.time.LocalDateTime.class);

    public final QGambleSeason gambleSeason;

    public final EnumPath<kr.kickon.api.global.common.enums.GameStatus> gameStatus = createEnum("gameStatus", kr.kickon.api.global.common.enums.GameStatus.class);

    public final NumberPath<Integer> homeScore = createNumber("homeScore", Integer.class);

    public final QTeam homeTeam;

    //inherited
    public final StringPath id = _super.id;

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    public final StringPath round = createString("round");

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QGame(String variable) {
        this(Game.class, forVariable(variable), INITS);
    }

    public QGame(Path<? extends Game> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGame(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGame(PathMetadata metadata, PathInits inits) {
        this(Game.class, metadata, inits);
    }

    public QGame(Class<? extends Game> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.actualSeason = inits.isInitialized("actualSeason") ? new QActualSeason(forProperty("actualSeason"), inits.get("actualSeason")) : null;
        this.awayTeam = inits.isInitialized("awayTeam") ? new QTeam(forProperty("awayTeam")) : null;
        this.gambleSeason = inits.isInitialized("gambleSeason") ? new QGambleSeason(forProperty("gambleSeason"), inits.get("gambleSeason")) : null;
        this.homeTeam = inits.isInitialized("homeTeam") ? new QTeam(forProperty("homeTeam")) : null;
    }

}

