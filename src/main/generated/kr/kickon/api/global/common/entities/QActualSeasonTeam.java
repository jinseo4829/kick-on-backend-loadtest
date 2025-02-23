package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QActualSeasonTeam is a Querydsl query type for ActualSeasonTeam
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QActualSeasonTeam extends EntityPathBase<ActualSeasonTeam> {

    private static final long serialVersionUID = 229728173L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QActualSeasonTeam actualSeasonTeam = new QActualSeasonTeam("actualSeasonTeam");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final QActualSeason actualSeason;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath etc = _super.etc;

    //inherited
    public final StringPath id = _super.id;

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    public final QTeam team;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QActualSeasonTeam(String variable) {
        this(ActualSeasonTeam.class, forVariable(variable), INITS);
    }

    public QActualSeasonTeam(Path<? extends ActualSeasonTeam> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QActualSeasonTeam(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QActualSeasonTeam(PathMetadata metadata, PathInits inits) {
        this(ActualSeasonTeam.class, metadata, inits);
    }

    public QActualSeasonTeam(Class<? extends ActualSeasonTeam> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.actualSeason = inits.isInitialized("actualSeason") ? new QActualSeason(forProperty("actualSeason"), inits.get("actualSeason")) : null;
        this.team = inits.isInitialized("team") ? new QTeam(forProperty("team"), inits.get("team")) : null;
    }

}

