package kr.kickon.api.domain.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kr.kickon.api.admin.user.dto.UserListDTO;
import kr.kickon.api.admin.user.request.UserFilterRequest;
import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.user.request.PatchUserRequest;
import kr.kickon.api.domain.user.request.PrivacyUpdateRequest;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.oauth.dto.OAuth2UserInfo;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.*;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements BaseService<User> {
    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final TeamService teamService;
    private final UserFavoriteTeamService userFavoriteTeamService;
    private final AwsFileReferenceService awsFileReferenceService;
    private final AwsService awsService;

    // region {findByPk} 사용자 PK 기준으로 유저를 조회합니다.
    /**
     * 사용자 PK 기준으로 유저를 조회합니다.
     * - status는 ACTIVATED인 사용자만 조회합니다.
     */
    public User findByPk(Long pk){
        BooleanExpression predicate = QUser.user.pk.eq(pk).and(QUser.user.status.eq(DataStatus.ACTIVATED));
        Optional<User> userEntity = userRepository.findOne(predicate);
        return userEntity.orElse(null);
    }
    // endregion

    // region {findById} 사용자 UUID 기준으로 유저를 조회합니다.
    /**
     * 사용자 UUID 기준으로 유저를 조회합니다.
     * - status는 ACTIVATED인 사용자만 조회합니다.
     */
    @Override
    public User findById(String uuid) {
        BooleanExpression predicate = QUser.user.id.eq(uuid).and(QUser.user.status.eq(DataStatus.ACTIVATED));
        Optional<User> userEntity = userRepository.findOne(predicate);
        return userEntity.orElse(null);
    }
    // endregion

    // region {findByEmail} 이메일을 기준으로 유저를 조회합니다.
    /**
     * 이메일을 기준으로 유저를 조회합니다.
     * - 상태는 ACTIVATED인 사용자만 조회합니다.
     */
    public  Optional<User> findByEmail(String email){
        BooleanExpression predicate = QUser.user.email.eq(email).and(QUser.user.status.eq(DataStatus.ACTIVATED));
        return userRepository.findOne(predicate);
    }
    // endregion

    // region {findByProviderAndProviderId} 소셜 로그인 정보를 기반으로 유저를 조회합니다.
    /**
     * 소셜 로그인 정보를 기반으로 유저를 조회합니다.
     * - provider와 providerId가 일치하는 가장 최근 유저 1명을 조회합니다.
     */
    public Optional<User> findByProviderAndProviderId(ProviderType provider, String providerId){
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
    // endregion

    // region {getUserListByStatus} 상태값(DataStatus)에 해당하는 유저 목록을 조회합니다.
    /**
     * 상태값(DataStatus)에 해당하는 유저 목록을 조회합니다.
     */
    public List<User> getUserListByStatus(DataStatus status){
        // QueryDSL Predicate 생성
        BooleanExpression predicate = QUser.user.status.eq(status);
        return (List<User>) userRepository.findAll(predicate);
    }
    // endregion

    // region {getUserListByEmail} 이메일이 일치하는 유저 목록을 조회합니다.
    /**
     * 이메일이 일치하는 유저 목록을 조회합니다.
     */
//    public List<User> getUserListByEmail(String email){
//        // JPAQueryFactory
//        return queryFactory.selectFrom(QUser.user)
//                .where(QUser.user.email.eq(email))
//                .fetch();
//    }
    // endregion

    // region {getUserListByFilter} 이메일, 닉네임 조건으로 사용자 목록을 필터링하여 페이징 조회합니다.
    /**
     * 이메일, 닉네임 조건으로 사용자 목록을 필터링하여 페이징 조회합니다.
     * - 상태는 ACTIVATED인 사용자만 조회합니다.
     */
    public Page<User> getUserListByFilter(UserFilterRequest request, Pageable pageable) {
        QUser user = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            builder.and(user.email.containsIgnoreCase(request.getEmail()));
        }

        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            builder.and(user.nickname.containsIgnoreCase(request.getNickname()));
        }

        builder.and(user.status.eq(DataStatus.ACTIVATED));

        // total count
        long total = queryFactory
                .select(user.count())
                .from(user)
                .where(builder)
                .fetchOne();

        // content
        List<User> content = queryFactory
                .selectFrom(user)
                .where(builder)
                .orderBy(user.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }
    // endregion

    // region {existsByEmailAndProvider} 이메일 및 소셜 제공자 기준 존재 여부 확인
    /**
     * 특정 이메일과 소셜 로그인 제공자(Provider)에 해당하는 사용자가 존재하는지 확인합니다.
     */
    public boolean existsByEmailAndProvider(String email, ProviderType providerType) {
        return userRepository.existsByEmailAndProvider(email, providerType);
    }
    // endregion

    // region {deleteUser} 유저 삭제
    public void deleteUser(User user){
        user.setStatus(DataStatus.DEACTIVATED);
        saveUser(user);
    }
    // endregion

    // region {updateUser} 사용자 정보 업데이트 (닉네임, 응원팀, 프로필 이미지)
    /**
     * 사용자 정보를 업데이트합니다.
     * - 닉네임 변경 (중복 체크 포함)
     * - 응원팀 우선순위 변경 (6개월 제한 조건 포함)
     * - 프로필 이미지 변경 (기존 이미지 삭제 및 새로운 이미지 등록)
     */
    @Transactional
    public void updateUser(User user, PatchUserRequest request) {
        if ( request.getNickname()!=null) {
            if(user.getNickname() == null || !user.getNickname().equals(request.getNickname())){
                // 닉네임 중복 검사
                boolean isDuplicated = existsByNickname(request.getNickname());
                if (isDuplicated) {
                    throw new BadRequestException(ResponseCode.DUPLICATED_NICKNAME);
                }
                user.setNickname(request.getNickname());
            }
        }

        // 팀 우선순위 수정
        if (request.getTeams() != null) {
            List<Long> requestedTeamPks = request.getTeams();
            List<UserFavoriteTeam> existingList = userFavoriteTeamService.findAllByUserPk(user.getPk());

            // 기존 활성화된 팀 목록 (PK만 추출, 순서 무시)
            Set<Long> currentTeamPkSet = existingList.stream()
                    .filter(uf -> uf.getStatus() == DataStatus.ACTIVATED)
                    .map(uf -> uf.getTeam().getPk())
                    .collect(Collectors.toSet());

            Set<Long> requestedTeamPkSet = new HashSet<>(requestedTeamPks);

            // ✅ 팀 구성(PK 집합)이 달라진 경우에만 제한 검사 수행
            if (!currentTeamPkSet.equals(requestedTeamPkSet)) {
                Optional<LocalDateTime> latestUpdatedAt = existingList.stream()
                        .filter(uf -> uf.getStatus() == DataStatus.ACTIVATED)
                        .map(UserFavoriteTeam::getUpdatedAt)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo);

                if (latestUpdatedAt.isPresent() &&
                        latestUpdatedAt.get().plusMonths(6).isAfter(LocalDateTime.now())) {
                    throw new BadRequestException(ResponseCode.CANNOT_CHANGE_TEAM_YET);
                }
            }

            // 기존 팀 매핑
            Map<Long, UserFavoriteTeam> teamMap = existingList.stream()
                    .collect(Collectors.toMap(uf -> uf.getTeam().getPk(), Function.identity()));

            // 요청되지 않은 팀은 비활성화 처리
            for (UserFavoriteTeam oldUft : existingList) {
                if (!requestedTeamPkSet.contains(oldUft.getTeam().getPk())) {
                    oldUft.setStatus(DataStatus.DEACTIVATED);
                    userFavoriteTeamService.save(oldUft);
                }
            }

            // 요청된 팀들 우선순위에 따라 등록/업데이트
            for (int i = 0; i < requestedTeamPks.size(); i++) {
                Long teamPk = requestedTeamPks.get(i);
                Team team = teamService.findByPk(teamPk);
                if (team == null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);

                UserFavoriteTeam uft = teamMap.get(teamPk);
                if (uft == null) {
                    // 새 팀 추가
                    uft = UserFavoriteTeam.builder()
                            .user(user)
                            .team(team)
                            .priorityNum(i + 1)
                            .status(DataStatus.ACTIVATED)
                            .build();
                } else {
                    // 기존 팀 재활성화 및 우선순위 갱신
                    uft.setPriorityNum(i + 1);
                    uft.setStatus(DataStatus.ACTIVATED);
                }

                userFavoriteTeamService.save(uft);
            }
        }

        if(request.getProfileImageUrl()!=null){
            user.setProfileImageUrl(request.getProfileImageUrl());
            // 1. "amazonaws.com/" 기준으로 key 추출
            String[] parts = request.getProfileImageUrl().split("amazonaws.com/");
            if (parts.length < 2) {
                throw new BadRequestException(ResponseCode.INVALID_REQUEST);
            }

            String encodedKey = parts[1];
            String decodedKey = URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);

            // 2. 기존 이미지 삭제 처리
            List<AwsFileReference> awsFileReferences = awsFileReferenceService.findByUserPk(user.getPk());

            // 기존 프로필 사진이 있는 경우 삭제
            for(AwsFileReference awsFileReference : awsFileReferences){
                try (S3Client s3 = S3Client.builder().build()) {
                    awsService.deleteFileFromS3AndDb(s3, awsFileReference); // key가 저장된 컬럼명에 따라 다름
                }
            }

            // 3. 새로운 프로필 이미지 등록
            AwsFileReference newProfileImage = awsFileReferenceService.findByKey(decodedKey);
            newProfileImage.setUsedIn(UsedInType.USER_PROFILE);
            newProfileImage.setReferencePk(user.getPk());
            awsFileReferenceService.save(newProfileImage);
        }

        saveUser(user); // nickname이 변경됐을 수도 있으니까
    }
    // endregion

    // region {existsByNickname} 닉네임 중복 여부 확인
    public boolean existsByNickname(String nickname){
        return userRepository.existsByNicknameAndStatus(nickname, DataStatus.ACTIVATED);
    }
    //#endregion

    // region {save} 유저 저장
    public void saveUser(User user){
        userRepository.save(user);
    }
    // endregion

    // region {saveSocialUser} 소셜 로그인 유저 저장
    @Transactional
    public User saveSocialUser(OAuth2UserInfo oAuth2UserInfo){
        String id = uuidGenerator.generateUniqueUUID(this::findById);
        User UserEntity = User.builder()
                .id(id)
                .provider(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getProviderId())
                .email(oAuth2UserInfo.getEmail())
                .profileImageUrl(oAuth2UserInfo.getProfileImageUrl())
                .nickname(oAuth2UserInfo.getNickname())
                .userStatus(UserAccountStatus.DEFAULT)
                .build();
        return userRepository.save(UserEntity);
    }
    // endregion

    // region {updatePrivacy} Privacy 업데이트
    public void updatePrivacy(User user, PrivacyUpdateRequest request) {
        user.setPrivacyAgreedAt(request.getPrivacyAgreedAt());
        user.setMarketingAgreedAt(request.getMarketingAgreedAt());

        userRepository.save(user);
    }
    // endregion
}
