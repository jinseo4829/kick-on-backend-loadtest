package kr.kickon.api.domain.eventBoard;

import kr.kickon.api.global.common.entities.EventBoard;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventBoardRepository extends JpaRepository<EventBoard, Long>, QuerydslPredicateExecutor<EventBoard> {
    Page<EventBoard> findAllByStatus(DataStatus status, Pageable pageable);

    Page<EventBoard> findByIsDisplayedAndStatusOrderByOrderNumAsc(boolean isDisplayed, DataStatus status, Pageable pageable);

    Page<EventBoard> findByIsDisplayedAndStatusOrderByCreatedAtDesc(boolean isDisplayed, DataStatus status, Pageable pageable);
}