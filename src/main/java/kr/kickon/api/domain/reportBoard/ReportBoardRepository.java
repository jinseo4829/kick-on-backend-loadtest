package kr.kickon.api.domain.reportBoard;
import kr.kickon.api.global.common.entities.ReportBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportBoardRepository extends JpaRepository<ReportBoard, Long>, QuerydslPredicateExecutor<ReportBoard> {
}