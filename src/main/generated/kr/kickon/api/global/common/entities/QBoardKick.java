package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoardKick is a Querydsl query type for BoardKick
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoardKick extends EntityPathBase<BoardKick> {

    private static final long serialVersionUID = 1409221517L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBoardKick boardKick = new QBoardKick("boardKick");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final QBoard board;

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

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QBoardKick(String variable) {
        this(BoardKick.class, forVariable(variable), INITS);
    }

    public QBoardKick(Path<? extends BoardKick> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoardKick(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoardKick(PathMetadata metadata, PathInits inits) {
        this(BoardKick.class, metadata, inits);
    }

    public QBoardKick(Class<? extends BoardKick> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.board = inits.isInitialized("board") ? new QBoard(forProperty("board"), inits.get("board")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

