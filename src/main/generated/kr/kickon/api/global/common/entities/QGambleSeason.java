package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGambleSeason is a Querydsl query type for GambleSeason
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGambleSeason extends EntityPathBase<GambleSeason> {

    private static final long serialVersionUID = 1666790858L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGambleSeason gambleSeason = new QGambleSeason("gambleSeason");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final QActualSeason actualSeason;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    //inherited
    public final StringPath etc = _super.etc;

    public final DateTimePath<java.time.LocalDateTime> finishedAt = createDateTime("finishedAt", java.time.LocalDateTime.class);

    //inherited
    public final StringPath id = _super.id;

    public final EnumPath<kr.kickon.api.global.common.enums.OperatingStatus> operatingStatus = createEnum("operatingStatus", kr.kickon.api.global.common.enums.OperatingStatus.class);

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QGambleSeason(String variable) {
        this(GambleSeason.class, forVariable(variable), INITS);
    }

    public QGambleSeason(Path<? extends GambleSeason> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGambleSeason(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGambleSeason(PathMetadata metadata, PathInits inits) {
        this(GambleSeason.class, metadata, inits);
    }

    public QGambleSeason(Class<? extends GambleSeason> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.actualSeason = inits.isInitialized("actualSeason") ? new QActualSeason(forProperty("actualSeason"), inits.get("actualSeason")) : null;
    }

}

