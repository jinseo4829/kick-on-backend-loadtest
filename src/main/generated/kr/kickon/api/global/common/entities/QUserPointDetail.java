package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserPointDetail is a Querydsl query type for UserPointDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserPointDetail extends EntityPathBase<UserPointDetail> {

    private static final long serialVersionUID = 1612022199L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserPointDetail userPointDetail = new QUserPointDetail("userPointDetail");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath etc = _super.etc;

    //inherited
    public final StringPath id = _super.id;

    public final QUserPointDetail parentUserPointDetail;

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    public final EnumPath<kr.kickon.api.global.common.enums.PointStatus> pointStatus = createEnum("pointStatus", kr.kickon.api.global.common.enums.PointStatus.class);

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public final QUserPointEvent userPointEvent;

    public QUserPointDetail(String variable) {
        this(UserPointDetail.class, forVariable(variable), INITS);
    }

    public QUserPointDetail(Path<? extends UserPointDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserPointDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserPointDetail(PathMetadata metadata, PathInits inits) {
        this(UserPointDetail.class, metadata, inits);
    }

    public QUserPointDetail(Class<? extends UserPointDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parentUserPointDetail = inits.isInitialized("parentUserPointDetail") ? new QUserPointDetail(forProperty("parentUserPointDetail"), inits.get("parentUserPointDetail")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
        this.userPointEvent = inits.isInitialized("userPointEvent") ? new QUserPointEvent(forProperty("userPointEvent"), inits.get("userPointEvent")) : null;
    }

}

