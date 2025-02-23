package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QActualSeasonRanking is a Querydsl query type for ActualSeasonRanking
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QActualSeasonRanking extends EntityPathBase<ActualSeasonRanking> {

    private static final long serialVersionUID = 71625958L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QActualSeasonRanking actualSeasonRanking = new QActualSeasonRanking("actualSeasonRanking");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final QActualSeason actualSeason;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> draws = createNumber("draws", Integer.class);

    //inherited
    public final StringPath etc = _super.etc;

    public final NumberPath<Integer> gameNum = createNumber("gameNum", Integer.class);

    //inherited
    public final StringPath id = _super.id;

    public final NumberPath<Integer> loses = createNumber("loses", Integer.class);

    public final NumberPath<Integer> lostScores = createNumber("lostScores", Integer.class);

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    public final NumberPath<Integer> points = createNumber("points", Integer.class);

    public final NumberPath<Integer> rankOrder = createNumber("rankOrder", Integer.class);

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    public final QTeam team;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> wins = createNumber("wins", Integer.class);

    public final NumberPath<Integer> wonScores = createNumber("wonScores", Integer.class);

    public QActualSeasonRanking(String variable) {
        this(ActualSeasonRanking.class, forVariable(variable), INITS);
    }

    public QActualSeasonRanking(Path<? extends ActualSeasonRanking> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QActualSeasonRanking(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QActualSeasonRanking(PathMetadata metadata, PathInits inits) {
        this(ActualSeasonRanking.class, metadata, inits);
    }

    public QActualSeasonRanking(Class<? extends ActualSeasonRanking> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.actualSeason = inits.isInitialized("actualSeason") ? new QActualSeason(forProperty("actualSeason"), inits.get("actualSeason")) : null;
        this.team = inits.isInitialized("team") ? new QTeam(forProperty("team"), inits.get("team")) : null;
    }

}

