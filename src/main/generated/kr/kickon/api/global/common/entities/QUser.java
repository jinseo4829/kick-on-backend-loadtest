package kr.kickon.api.global.common.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -389276182L;

    public static final QUser user = new QUser("user");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    //inherited
    public final StringPath etc = _super.etc;

    //inherited
    public final StringPath id = _super.id;

    public final DateTimePath<java.time.LocalDateTime> marketingAgreedAt = createDateTime("marketingAgreedAt", java.time.LocalDateTime.class);

    public final StringPath nickname = createString("nickname");

    //inherited
    public final NumberPath<Long> pk = _super.pk;

    public final DateTimePath<java.time.LocalDateTime> privacyAgreedAt = createDateTime("privacyAgreedAt", java.time.LocalDateTime.class);

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<kr.kickon.api.global.common.enums.ProviderType> provider = createEnum("provider", kr.kickon.api.global.common.enums.ProviderType.class);

    public final StringPath providerId = createString("providerId");

    //inherited
    public final EnumPath<kr.kickon.api.global.common.enums.DataStatus> status = _super.status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final EnumPath<kr.kickon.api.global.common.enums.UserAccountStatus> userStatus = createEnum("userStatus", kr.kickon.api.global.common.enums.UserAccountStatus.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

