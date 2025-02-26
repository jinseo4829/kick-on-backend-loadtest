package kr.kickon.api.domain.team;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kr.kickon.api.domain.user.UserRepository;
import kr.kickon.api.domain.user.request.PrivacyUpdateRequest;
import kr.kickon.api.global.auth.oauth.dto.OAuth2UserInfo;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.QTeam;
import kr.kickon.api.global.common.entities.QUser;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ProviderType;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.common.enums.UserAccountStatus;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamService implements BaseService<Team> {
    private final TeamRepository teamRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
//    public List<User> findUserByEmail(String email){
//        // JPAQueryFactory
//        return queryFactory.selectFrom(QUser.user)
//                .where(QUser.user.email.eq(email))
//                .fetch();
//    }
    @Override
    public Team findById(String uuid) {
        BooleanExpression predicate = QTeam.team.id.eq(uuid).and(QTeam.team.status.eq(DataStatus.ACTIVATED));
        Optional<Team> team = teamRepository.findOne(predicate);
        if(team.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
        return team.get();
    }

    @Override
    public Team findByPk(Long pk) {
        BooleanExpression predicate = QTeam.team.pk.eq(pk).and(QTeam.team.status.eq(DataStatus.ACTIVATED));
        Optional<Team> team = teamRepository.findOne(predicate);
        if(team.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
        return team.get();
    }
}
