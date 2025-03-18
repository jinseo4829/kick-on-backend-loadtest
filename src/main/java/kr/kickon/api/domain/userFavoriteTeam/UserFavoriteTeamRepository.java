package kr.kickon.api.domain.userFavoriteTeam;

import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFavoriteTeamRepository extends JpaRepository<UserFavoriteTeam, Long>, QuerydslPredicateExecutor<UserFavoriteTeam> {
}