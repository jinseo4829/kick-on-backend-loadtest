package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserPointEvent is a Querydsl query type for UserPointEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserPointEvent extends EntityPathBase<UserPointEvent> {

    private static final long serialVersionUID = 469058676L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserPointEvent userPointEvent = new QUserPointEvent("userPointEvent");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final EnumPath<kr.kickon.api.global.common.enums.PointCategory> category = createEnum("category", kr.kickon.api.global.common.enums.PointCategory.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath etc = _super.etc;

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    //inherited
    public final StringPath id = _super.id;

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    public final EnumPath<kr.kickon.api.global.common.enums.PointStatus> pointStatus = createEnum("pointStatus", kr.kickon.api.global.common.enums.PointStatus.class);

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QUserPointEvent(String variable) {
        this(UserPointEvent.class, forVariable(variable), INITS);
    }

    public QUserPointEvent(Path<? extends UserPointEvent> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserPointEvent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserPointEvent(PathMetadata metadata, PathInits inits) {
        this(UserPointEvent.class, metadata, inits);
    }

    public QUserPointEvent(Class<? extends UserPointEvent> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

