package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNewsKick is a Querydsl query type for NewsKick
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNewsKick extends EntityPathBase<NewsKick> {

    private static final long serialVersionUID = -1417633192L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNewsKick newsKick = new QNewsKick("newsKick");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath etc = _super.etc;

    //inherited
    public final StringPath id = _super.id;

    public final QNews news;

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QNewsKick(String variable) {
        this(NewsKick.class, forVariable(variable), INITS);
    }

    public QNewsKick(Path<? extends NewsKick> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNewsKick(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNewsKick(PathMetadata metadata, PathInits inits) {
        this(NewsKick.class, metadata, inits);
    }

    public QNewsKick(Class<? extends NewsKick> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.news = inits.isInitialized("news") ? new QNews(forProperty("news"), inits.get("news")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

