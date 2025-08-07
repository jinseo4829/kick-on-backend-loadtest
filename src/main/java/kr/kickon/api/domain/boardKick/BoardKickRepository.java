package kr.kickon.api.domain.boardKick;
import java.time.LocalDateTime;
import kr.kickon.api.global.common.entities.BoardKick;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardKickRepository extends JpaRepository<BoardKick, Long>, QuerydslPredicateExecutor<BoardKick> {

  Long countByBoard_PkAndStatus(Long boardPk, DataStatus status);

  // 48시간 이내의 활성화된 BoardKick 수
  long countByBoard_PkAndCreatedAtAfterAndStatus(Long boardPk, LocalDateTime after, DataStatus status);
}