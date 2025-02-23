package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReportNews is a Querydsl query type for ReportNews
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReportNews extends EntityPathBase<ReportNews> {

    private static final long serialVersionUID = 929331462L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReportNews reportNews = new QReportNews("reportNews");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath etc = _super.etc;

    //inherited
    public final StringPath id = _super.id;

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    public final StringPath reason = createString("reason");

    public final QNews reportedNews;

    public final EnumPath<kr.kickon.api.global.common.enums.ReportStatus> reportStatus = createEnum("reportStatus", kr.kickon.api.global.common.enums.ReportStatus.class);

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QReportNews(String variable) {
        this(ReportNews.class, forVariable(variable), INITS);
    }

    public QReportNews(Path<? extends ReportNews> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReportNews(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReportNews(PathMetadata metadata, PathInits inits) {
        this(ReportNews.class, metadata, inits);
    }

    public QReportNews(Class<? extends ReportNews> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reportedNews = inits.isInitialized("reportedNews") ? new QNews(forProperty("reportedNews"), inits.get("reportedNews")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

