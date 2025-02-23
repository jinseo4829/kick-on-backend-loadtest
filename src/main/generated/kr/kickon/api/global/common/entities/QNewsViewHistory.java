package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNewsViewHistory is a Querydsl query type for NewsViewHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNewsViewHistory extends EntityPathBase<NewsViewHistory> {

    private static final long serialVersionUID = -1615876643L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNewsViewHistory newsViewHistory = new QNewsViewHistory("newsViewHistory");

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

    public QNewsViewHistory(String variable) {
        this(NewsViewHistory.class, forVariable(variable), INITS);
    }

    public QNewsViewHistory(Path<? extends NewsViewHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNewsViewHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNewsViewHistory(PathMetadata metadata, PathInits inits) {
        this(NewsViewHistory.class, metadata, inits);
    }

    public QNewsViewHistory(Class<? extends NewsViewHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.news = inits.isInitialized("news") ? new QNews(forProperty("news"), inits.get("news")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

