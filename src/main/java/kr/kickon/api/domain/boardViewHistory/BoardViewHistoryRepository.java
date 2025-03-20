package kr.kickon.api.domain.boardViewHistory;
import kr.kickon.api.global.common.entities.BoardViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardViewHistoryRepository extends JpaRepository<BoardViewHistory, Long>, QuerydslPredicateExecutor<BoardViewHistory> {
}