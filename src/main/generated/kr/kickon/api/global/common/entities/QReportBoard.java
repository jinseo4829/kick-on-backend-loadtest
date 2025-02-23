package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReportBoard is a Querydsl query type for ReportBoard
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReportBoard extends EntityPathBase<ReportBoard> {

    private static final long serialVersionUID = -1266301165L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReportBoard reportBoard = new QReportBoard("reportBoard");

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

    public final QBoard reportedBoard;

    public final EnumPath<kr.kickon.api.global.common.enums.ReportStatus> reportStatus = createEnum("reportStatus", kr.kickon.api.global.common.enums.ReportStatus.class);

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QReportBoard(String variable) {
        this(ReportBoard.class, forVariable(variable), INITS);
    }

    public QReportBoard(Path<? extends ReportBoard> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReportBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReportBoard(PathMetadata metadata, PathInits inits) {
        this(ReportBoard.class, metadata, inits);
    }

    public QReportBoard(Class<? extends ReportBoard> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reportedBoard = inits.isInitialized("reportedBoard") ? new QBoard(forProperty("reportedBoard"), inits.get("reportedBoard")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

