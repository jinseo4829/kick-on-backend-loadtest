package kr.kickon.api.domain.boardReplyKick;
import kr.kickon.api.global.common.entities.BoardKick;
import kr.kickon.api.global.common.entities.BoardReplyKick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardReplyKickRepository extends JpaRepository<BoardReplyKick, Long>, QuerydslPredicateExecutor<BoardReplyKick> {
}