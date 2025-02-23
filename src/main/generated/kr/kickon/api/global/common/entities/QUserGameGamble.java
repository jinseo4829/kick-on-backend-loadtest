package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserGameGamble is a Querydsl query type for UserGameGamble
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserGameGamble extends EntityPathBase<UserGameGamble> {

    private static final long serialVersionUID = -506777148L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserGameGamble userGameGamble = new QUserGameGamble("userGameGamble");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath etc = _super.etc;

    public final EnumPath<kr.kickon.api.global.common.enums.GambleStatus> gambleStatus = createEnum("gambleStatus", kr.kickon.api.global.common.enums.GambleStatus.class);

    public final QGame game;

    //inherited
    public final StringPath id = _super.id;

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    public final NumberPath<Integer> predictedAwayScore = createNumber("predictedAwayScore", Integer.class);

    public final NumberPath<Integer> predictedHomeScore = createNumber("predictedHomeScore", Integer.class);

    public final EnumPath<kr.kickon.api.global.common.enums.PredictedResult> predictedResult = createEnum("predictedResult", kr.kickon.api.global.common.enums.PredictedResult.class);

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    public final QTeam supportingTeam;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QUserGameGamble(String variable) {
        this(UserGameGamble.class, forVariable(variable), INITS);
    }

    public QUserGameGamble(Path<? extends UserGameGamble> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserGameGamble(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserGameGamble(PathMetadata metadata, PathInits inits) {
        this(UserGameGamble.class, metadata, inits);
    }

    public QUserGameGamble(Class<? extends UserGameGamble> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.game = inits.isInitialized("game") ? new QGame(forProperty("game"), inits.get("game")) : null;
        this.supportingTeam = inits.isInitialized("supportingTeam") ? new QTeam(forProperty("supportingTeam"), inits.get("supportingTeam")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

