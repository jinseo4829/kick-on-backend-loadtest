package kr.kickon.api.domain.userGameGamble;

import kr.kickon.api.global.common.entities.UserGameGamble;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGameGambleRepository extends JpaRepository<UserGameGamble, Long>, QuerydslPredicateExecutor<UserGameGamble> {
    Page<UserGameGamble> findAllByGamePkAndStatusOrderByCreatedAtDesc(Long gamePk, DataStatus status, Pageable pageable);
}