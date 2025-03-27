package kr.kickon.api.domain.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.user.request.PatchUserRequest;
import kr.kickon.api.domain.user.request.PrivacyUpdateRequest;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.oauth.dto.OAuth2UserInfo;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.QUser;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
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
public class UserService implements BaseService<User> {
    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final UserFavoriteTeamService userFavoriteTeamService;

    public List<User> findUsersByStatus(DataStatus status){
        // QueryDSL Predicate 생성
        BooleanExpression predicate = QUser.user.status.eq(status);
        return (List<User>) userRepository.findAll(predicate);
    }

//    public List<User> findUserByEmail(String email){
//        // JPAQueryFactory
//        return queryFactory.selectFrom(QUser.user)
//                .where(QUser.user.email.eq(email))
//                .fetch();
//    }

    public  Optional<User> findUserByEmail(String email){
        BooleanExpression predicate = QUser.user.email.eq(email).and(QUser.user.status.eq(DataStatus.ACTIVATED));
        return userRepository.findOne(predicate);
    }

    public Optional<User> findUserByProviderAndProviderId(ProviderType provider, String providerId){
        BooleanExpression predicate = QUser.user.provider.eq(provider).and(QUser.user.status.eq(DataStatus.ACTIVATED));
        return userRepository.findOne(predicate);
    }

    public void saveUser(User user){
        userRepository.save(user);
    }

    public User findByPk(Long pk){
        BooleanExpression predicate = QUser.user.pk.eq(pk).and(QUser.user.status.eq(DataStatus.ACTIVATED));
        Optional<User> user = userRepository.findOne(predicate);
        return user.orElse(null);
    }

    @Transactional
    public User saveSocialUser(OAuth2UserInfo oAuth2UserInfo){
        String id = uuidGenerator.generateUniqueUUID(this::findById);
        User saveUser = User.builder()
                .id(id)
                .provider(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getProviderId())
                .email(oAuth2UserInfo.getEmail())
                .profileImageUrl(oAuth2UserInfo.getProfileImageUrl())
                .nickname(oAuth2UserInfo.getNickname())
                .userStatus(UserAccountStatus.DEFAULT)
                .build();
        return userRepository.save(saveUser);
    }

    @Override
    public User findById(String uuid) {
        BooleanExpression predicate = QUser.user.id.eq(uuid).and(QUser.user.status.eq(DataStatus.ACTIVATED));
        Optional<User> user = userRepository.findOne(predicate);
        return user.orElse(null);
    }

    public void updatePrivacy(User user, PrivacyUpdateRequest request) {
        user.setPrivacyAgreedAt(request.getPrivacyAgreedAt());
        user.setMarketingAgreedAt(request.getMarketingAgreedAt());

        userRepository.save(user);
    }

    public void updateUser(User user, Team team){
        user.setNickname(request.getNickname());
        UserFavoriteTeam userFavoriteTeam = null;
        if(request.getTeam()!=null){

            userFavoriteTeam = userFavoriteTeamService.findByUserPk(user.getPk());
            user
        }
        userRepository.save(user);
    }
}
