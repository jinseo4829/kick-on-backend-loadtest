package kr.kickon.api.domain.boardKick;
import kr.kickon.api.global.common.entities.BoardKick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardKickRepository extends JpaRepository<BoardKick, Long>, QuerydslPredicateExecutor<BoardKick> {
}