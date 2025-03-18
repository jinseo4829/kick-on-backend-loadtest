package kr.kickon.api.domain.eventBoard;

import kr.kickon.api.global.common.entities.EventBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EventBoardRepository extends JpaRepository<EventBoard, Long>, QuerydslPredicateExecutor<EventBoard> {
}