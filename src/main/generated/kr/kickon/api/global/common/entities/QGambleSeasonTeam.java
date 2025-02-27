package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGambleSeasonTeam is a Querydsl query type for GambleSeasonTeam
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGambleSeasonTeam extends EntityPathBase<GambleSeasonTeam> {

    private static final long serialVersionUID = 83687239L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGambleSeasonTeam gambleSeasonTeam = new QGambleSeasonTeam("gambleSeasonTeam");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath etc = _super.etc;

    public final QGambleSeason gambleSeason;

    //inherited
    public final StringPath id = _super.id;

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    public final QTeam team;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QGambleSeasonTeam(String variable) {
        this(GambleSeasonTeam.class, forVariable(variable), INITS);
    }

    public QGambleSeasonTeam(Path<? extends GambleSeasonTeam> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGambleSeasonTeam(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGambleSeasonTeam(PathMetadata metadata, PathInits inits) {
        this(GambleSeasonTeam.class, metadata, inits);
    }

    public QGambleSeasonTeam(Class<? extends GambleSeasonTeam> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.gambleSeason = inits.isInitialized("gambleSeason") ? new QGambleSeason(forProperty("gambleSeason"), inits.get("gambleSeason")) : null;
        this.team = inits.isInitialized("team") ? new QTeam(forProperty("team")) : null;
    }

}

