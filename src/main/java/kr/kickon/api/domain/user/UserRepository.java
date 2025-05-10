package kr.kickon.api.domain.user;

import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {
    boolean existsByNicknameAndStatus(String nickname, DataStatus status);
}