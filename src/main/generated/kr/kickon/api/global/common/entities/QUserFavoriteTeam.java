package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserFavoriteTeam is a Querydsl query type for UserFavoriteTeam
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserFavoriteTeam extends EntityPathBase<UserFavoriteTeam> {

    private static final long serialVersionUID = 460010787L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserFavoriteTeam userFavoriteTeam = new QUserFavoriteTeam("userFavoriteTeam");

    public final QBaseEntity _super = new QBaseEntity(this);

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

    public final QUser user;

    public QUserFavoriteTeam(String variable) {
        this(UserFavoriteTeam.class, forVariable(variable), INITS);
    }

    public QUserFavoriteTeam(Path<? extends UserFavoriteTeam> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserFavoriteTeam(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserFavoriteTeam(PathMetadata metadata, PathInits inits) {
        this(UserFavoriteTeam.class, metadata, inits);
    }

    public QUserFavoriteTeam(Class<? extends UserFavoriteTeam> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.team = inits.isInitialized("team") ? new QTeam(forProperty("team"), inits.get("team")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

