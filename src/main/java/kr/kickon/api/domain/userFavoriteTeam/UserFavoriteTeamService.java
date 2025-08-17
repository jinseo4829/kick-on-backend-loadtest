package kr.kickon.api.domain.userFavoriteTeam;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.QUser;
import kr.kickon.api.global.common.entities.QUserFavoriteTeam;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
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
public class UserFavoriteTeamService implements BaseService<UserFavoriteTeam> {
    private final UserFavoriteTeamRepository userFavoriteTeamRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final QUserFavoriteTeam qUserFavoriteTeam = QUserFavoriteTeam.userFavoriteTeam;

    @Override
    public UserFavoriteTeam findById(String uuid) {
        BooleanExpression predicate = qUserFavoriteTeam.id.eq(uuid).and(qUserFavoriteTeam.status.eq(DataStatus.ACTIVATED));
        Optional<UserFavoriteTeam> userFavoriteTeam = userFavoriteTeamRepository.findOne(predicate);
        return userFavoriteTeam.orElse(null);
    }

    @Override
    public UserFavoriteTeam findByPk(Long pk) {
        BooleanExpression predicate = qUserFavoriteTeam.pk.eq(pk).and(qUserFavoriteTeam.status.eq(DataStatus.ACTIVATED));
        Optional<UserFavoriteTeam> userFavoriteTeam =userFavoriteTeamRepository.findOne(predicate);
        return userFavoriteTeam.orElse(null);
    }

    public UserFavoriteTeam findByUserPk(long pk){
        BooleanExpression predicate = qUserFavoriteTeam.user.pk.eq(pk)
                .and(qUserFavoriteTeam.status.eq(DataStatus.ACTIVATED)).and(qUserFavoriteTeam.team.status.eq(DataStatus.ACTIVATED));
        Optional<UserFavoriteTeam> userFavoriteTeam =userFavoriteTeamRepository.findOne(predicate);
        return userFavoriteTeam.orElse(null);
    }

    public List<UserFavoriteTeam> findTop3ByUserPkOrderByPriorityNumAsc(Long userPk){
        return userFavoriteTeamRepository.findTop3ByUser_PkAndStatusAndTeam_StatusOrderByPriorityNumAsc(userPk, DataStatus.ACTIVATED, DataStatus.ACTIVATED);
    }

    public List<UserFavoriteTeam> findAllByUserPk(Long userPk){
        return userFavoriteTeamRepository.findAllByUserPkAndStatus(userPk, DataStatus.ACTIVATED);
    }

    @Transactional
    public void save(UserFavoriteTeam userFavoriteTeam) {
        userFavoriteTeamRepository.save(userFavoriteTeam);
    }

     /*
     * 특정 팀을 즐겨찾기로 등록한 유저 수(팬 수)를 반환
     * @param teamPk 팀 PK
     * @return 팬 수
     */
    public Integer countFansByTeamPk(Long teamPk) {
        QUserFavoriteTeam ft = QUserFavoriteTeam.userFavoriteTeam;
        QUser u = QUser.user;

        Long count = queryFactory
            .select(ft.count())
            .from(ft)
            .join(ft.user, u) // 유저 활성 상태인 지 확인
            .where(
                ft.team.pk.eq(teamPk),
                ft.status.eq(DataStatus.ACTIVATED),
                ft.team.status.eq(DataStatus.ACTIVATED),
                u.status.eq(DataStatus.ACTIVATED)
            )
            .fetchOne(); // 없으면 null

        return count != null ? count.intValue() : 0;
    }

    public List<User> findUsersByTeamPk(Long teamPk) {
        QUserFavoriteTeam ft = QUserFavoriteTeam.userFavoriteTeam;
        QUser u = QUser.user;

        return queryFactory
                .select(u)
                .from(ft)
                .join(ft.user, u)
                .where(
                        ft.team.pk.eq(teamPk),
                        ft.status.eq(DataStatus.ACTIVATED),
                        ft.team.status.eq(DataStatus.ACTIVATED),
                        u.status.eq(DataStatus.ACTIVATED)
                )
                .fetch();
    }

    public boolean isUserFavoriteTeam(User user, Long teamPk) {
        return userFavoriteTeamRepository.existsByUserAndTeamPkAndStatus(user, teamPk, DataStatus.ACTIVATED);
    }

}
