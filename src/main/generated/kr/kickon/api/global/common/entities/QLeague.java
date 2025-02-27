package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLeague is a Querydsl query type for League
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLeague extends EntityPathBase<League> {

    private static final long serialVersionUID = -702973810L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLeague league = new QLeague("league");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final NumberPath<Long> apiId = createNumber("apiId", Long.class);

    public final QCountry country;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Integer> division = createNumber("division", Integer.class);

    public final StringPath enName = createString("enName");

    //inherited
    public final StringPath etc = _super.etc;

    //inherited
    public final StringPath id = _super.id;

    public final StringPath krName = createString("krName");

    public final StringPath logoUrl = createString("logoUrl");

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    public final EnumPath<kr.kickon.api.global.common.enums.LeagueType> type = createEnum("type", kr.kickon.api.global.common.enums.LeagueType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLeague(String variable) {
        this(League.class, forVariable(variable), INITS);
    }

    public QLeague(Path<? extends League> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLeague(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLeague(PathMetadata metadata, PathInits inits) {
        this(League.class, metadata, inits);
    }

    public QLeague(Class<? extends League> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.country = inits.isInitialized("country") ? new QCountry(forProperty("country")) : null;
    }

}

