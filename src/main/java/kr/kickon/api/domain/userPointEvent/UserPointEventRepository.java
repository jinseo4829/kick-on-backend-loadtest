package kr.kickon.api.domain.userPointEvent;

import kr.kickon.api.global.common.entities.UserPointEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPointEventRepository extends JpaRepository<UserPointEvent, Long>, QuerydslPredicateExecutor<UserPointEvent> {
};
