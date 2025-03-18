package kr.kickon.api.domain.BoardReply;
import kr.kickon.api.global.common.entities.BoardReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardReplyRepository extends JpaRepository<BoardReply, Long>, QuerydslPredicateExecutor<BoardReply> {
}