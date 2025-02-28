package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QActualSeason is a Querydsl query type for ActualSeason
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QActualSeason extends EntityPathBase<ActualSeason> {

    private static final long serialVersionUID = 162382128L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QActualSeason actualSeason = new QActualSeason("actualSeason");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    //inherited
    public final StringPath etc = _super.etc;

    public final DatePath<java.time.LocalDate> finishedAt = createDate("finishedAt", java.time.LocalDate.class);

    //inherited
    public final StringPath id = _super.id;

    public final QLeague league;

    public final EnumPath<kr.kickon.api.global.common.enums.OperatingStatus> operatingStatus = createEnum("operatingStatus", kr.kickon.api.global.common.enums.OperatingStatus.class);

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    public final DatePath<java.time.LocalDate> startedAt = createDate("startedAt", java.time.LocalDate.class);

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public QActualSeason(String variable) {
        this(ActualSeason.class, forVariable(variable), INITS);
    }

    public QActualSeason(Path<? extends ActualSeason> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QActualSeason(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QActualSeason(PathMetadata metadata, PathInits inits) {
        this(ActualSeason.class, metadata, inits);
    }

    public QActualSeason(Class<? extends ActualSeason> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.league = inits.isInitialized("league") ? new QLeague(forProperty("league"), inits.get("league")) : null;
    }

}

