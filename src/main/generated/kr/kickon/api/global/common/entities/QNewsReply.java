package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNewsReply is a Querydsl query type for NewsReply
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNewsReply extends EntityPathBase<NewsReply> {

    private static final long serialVersionUID = -990597864L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNewsReply newsReply = new QNewsReply("newsReply");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath contents = createString("contents");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath etc = _super.etc;

    //inherited
    public final StringPath id = _super.id;

    public final QNewsReply parentNewsReply;

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QNewsReply(String variable) {
        this(NewsReply.class, forVariable(variable), INITS);
    }

    public QNewsReply(Path<? extends NewsReply> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNewsReply(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNewsReply(PathMetadata metadata, PathInits inits) {
        this(NewsReply.class, metadata, inits);
    }

    public QNewsReply(Class<? extends NewsReply> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parentNewsReply = inits.isInitialized("parentNewsReply") ? new QNewsReply(forProperty("parentNewsReply"), inits.get("parentNewsReply")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

