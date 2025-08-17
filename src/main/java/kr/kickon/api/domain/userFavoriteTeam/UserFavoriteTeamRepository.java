package kr.kickon.api.domain.userFavoriteTeam;

import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFavoriteTeamRepository extends JpaRepository<UserFavoriteTeam, Long>, QuerydslPredicateExecutor<UserFavoriteTeam> {
    List<UserFavoriteTeam> findTop3ByUser_PkAndStatusAndTeam_StatusOrderByPriorityNumAsc(Long userPk, DataStatus status, DataStatus teamStatus);
    List<UserFavoriteTeam> findAllByUserPkAndStatus(Long userPk, DataStatus status);
    boolean existsByUserAndTeamPkAndStatus(User user, Long teamPk, DataStatus status);
}