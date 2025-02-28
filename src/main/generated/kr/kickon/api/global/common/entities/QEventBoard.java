package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEventBoard is a Querydsl query type for EventBoard
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventBoard extends EntityPathBase<EventBoard> {

    private static final long serialVersionUID = -44434613L;

    public static final QEventBoard eventBoard = new QEventBoard("eventBoard");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath embeddedUrl = createString("embeddedUrl");

    //inherited
    public final StringPath etc = _super.etc;

    //inherited
    public final StringPath id = _super.id;

    public final BooleanPath isDisplayed = createBoolean("isDisplayed");

    public final NumberPath<Integer> orderNum = createNumber("orderNum", Integer.class);

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEventBoard(String variable) {
        super(EventBoard.class, forVariable(variable));
    }

    public QEventBoard(Path<? extends EventBoard> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEventBoard(PathMetadata metadata) {
        super(EventBoard.class, metadata);
    }

}

