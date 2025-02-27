package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGambleSeasonRanking is a Querydsl query type for GambleSeasonRanking
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGambleSeasonRanking extends EntityPathBase<GambleSeasonRanking> {

    private static final long serialVersionUID = 168032012L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGambleSeasonRanking gambleSeasonRanking = new QGambleSeasonRanking("gambleSeasonRanking");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath etc = _super.etc;

    public final QGambleSeason gambleSeason;

    public final NumberPath<Integer> gameNum = createNumber("gameNum", Integer.class);

    //inherited
    public final StringPath id = _super.id;

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    public final NumberPath<Integer> points = createNumber("points", Integer.class);

    public final NumberPath<Integer> rankOrder = createNumber("rankOrder", Integer.class);

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    public final QTeam team;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QGambleSeasonRanking(String variable) {
        this(GambleSeasonRanking.class, forVariable(variable), INITS);
    }

    public QGambleSeasonRanking(Path<? extends GambleSeasonRanking> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGambleSeasonRanking(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGambleSeasonRanking(PathMetadata metadata, PathInits inits) {
        this(GambleSeasonRanking.class, metadata, inits);
    }

    public QGambleSeasonRanking(Class<? extends GambleSeasonRanking> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.gambleSeason = inits.isInitialized("gambleSeason") ? new QGambleSeason(forProperty("gambleSeason"), inits.get("gambleSeason")) : null;
        this.team = inits.isInitialized("team") ? new QTeam(forProperty("team")) : null;
    }

}

