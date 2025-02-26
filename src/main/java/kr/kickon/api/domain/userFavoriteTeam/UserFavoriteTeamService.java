package kr.kickon.api.domain.userFavoriteTeam;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.QUser;
import kr.kickon.api.global.common.entities.QUserFavoriteTeam;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFavoriteTeamService implements BaseService<UserFavoriteTeam> {
    private final UserFavoriteTeamRepository userFavoriteTeamRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final QUserFavoriteTeam qUserFavoriteTeam = QUserFavoriteTeam.userFavoriteTeam;

    @Override
    public UserFavoriteTeam findById(String uuid) {
        BooleanExpression predicate = qUserFavoriteTeam.id.eq(uuid).and(qUserFavoriteTeam.status.eq(DataStatus.ACTIVATED));
        Optional<UserFavoriteTeam> userFavoriteTeam = userFavoriteTeamRepository.findOne(predicate);
        if(userFavoriteTeam.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_USER_FAVORITE_TEAM);
        return userFavoriteTeam.get();
    }

    @Override
    public UserFavoriteTeam findByPk(Long pk) {
        BooleanExpression predicate = qUserFavoriteTeam.pk.eq(pk).and(qUserFavoriteTeam.status.eq(DataStatus.ACTIVATED));
        Optional<UserFavoriteTeam> userFavoriteTeam =userFavoriteTeamRepository.findOne(predicate);
        if(userFavoriteTeam.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_USER_FAVORITE_TEAM);
        return userFavoriteTeam.get();
    }

    public UserFavoriteTeam findByUserPk(long pk){
        BooleanExpression predicate = qUserFavoriteTeam.user.pk.eq(pk)
                .and(qUserFavoriteTeam.status.eq(DataStatus.ACTIVATED));
        Optional<UserFavoriteTeam> userFavoriteTeam =userFavoriteTeamRepository.findOne(predicate);
        if(userFavoriteTeam.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_USER_FAVORITE_TEAM);
        return userFavoriteTeam.get();
    }
}
