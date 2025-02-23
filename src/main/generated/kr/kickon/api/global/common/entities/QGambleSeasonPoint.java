package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGambleSeasonPoint is a Querydsl query type for GambleSeasonPoint
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGambleSeasonPoint extends EntityPathBase<GambleSeasonPoint> {

    private static final long serialVersionUID = -1704051226L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGambleSeasonPoint gambleSeasonPoint = new QGambleSeasonPoint("gambleSeasonPoint");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final NumberPath<Integer> averagePoints = createNumber("averagePoints", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath etc = _super.etc;

    public final QGambleSeason gambleSeason;

    public final QGame game;

    //inherited
    public final StringPath id = _super.id;

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    public final QTeam team;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QGambleSeasonPoint(String variable) {
        this(GambleSeasonPoint.class, forVariable(variable), INITS);
    }

    public QGambleSeasonPoint(Path<? extends GambleSeasonPoint> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGambleSeasonPoint(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGambleSeasonPoint(PathMetadata metadata, PathInits inits) {
        this(GambleSeasonPoint.class, metadata, inits);
    }

    public QGambleSeasonPoint(Class<? extends GambleSeasonPoint> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.gambleSeason = inits.isInitialized("gambleSeason") ? new QGambleSeason(forProperty("gambleSeason"), inits.get("gambleSeason")) : null;
        this.game = inits.isInitialized("game") ? new QGame(forProperty("game"), inits.get("game")) : null;
        this.team = inits.isInitialized("team") ? new QTeam(forProperty("team"), inits.get("team")) : null;
    }

}

