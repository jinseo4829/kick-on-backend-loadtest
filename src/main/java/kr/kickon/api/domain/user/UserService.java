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
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ProviderType;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.common.enums.UserAccountStatus;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements BaseService<User> {
    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final TeamService teamService;
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

    public void deleteUser(User user){
        user.setStatus(DataStatus.DEACTIVATED);
        saveUser(user);
    }

    @Transactional
    public void updateUser(User user, PatchUserRequest request) {
        if (!user.getNickname().equals(request.getNickname())) {
            user.setNickname(request.getNickname());
        }

        if (request.getTeam() != null) {
            Team team = teamService.findByPk(request.getTeam());
            if (team == null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);

            UserFavoriteTeam uft = userFavoriteTeamService.findByUserPk(user.getPk());
            if (uft == null) {
                String id = uuidGenerator.generateUniqueUUID(userFavoriteTeamService::findById);
                uft = UserFavoriteTeam.builder().id(id).user(user).team(team).build();
            } else {
                uft.setTeam(team);
            }
            userFavoriteTeamService.save(uft);
        }

        saveUser(user); // nickname이 변경됐을 수도 있으니까
    }

    public  Optional<User> findUserByEmail(String email){
        BooleanExpression predicate = QUser.user.email.eq(email).and(QUser.user.status.eq(DataStatus.ACTIVATED));
        return userRepository.findOne(predicate);
    }

    public boolean existsByNickname(String nickname){
        return userRepository.existsByNicknameAndStatus(nickname, DataStatus.ACTIVATED);
    }

    public Optional<User> findUserByProviderAndProviderId(ProviderType provider, String providerId){
        QUser user = QUser.user;

        BooleanExpression predicate = user.provider.eq(provider)
                .and(user.providerId.eq(providerId));

        return Optional.ofNullable(
                queryFactory.selectFrom(user)
                        .where(predicate)
                        .orderBy(user.createdAt.desc()) // 최신순 정렬
                        .fetchFirst() // 하나만!
        );
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
}
