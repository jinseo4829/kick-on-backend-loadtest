package kr.kickon.api.domain.userGameGamble;

import kr.kickon.api.global.common.entities.UserGameGamble;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGameGambleRepository extends JpaRepository<UserGameGamble, Long>, QuerydslPredicateExecutor<UserGameGamble> {
}