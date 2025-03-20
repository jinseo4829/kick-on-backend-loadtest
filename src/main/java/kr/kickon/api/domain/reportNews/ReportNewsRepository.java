package kr.kickon.api.domain.reportNews;
import kr.kickon.api.global.common.entities.ReportBoard;
import kr.kickon.api.global.common.entities.ReportNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportNewsRepository extends JpaRepository<ReportNews, Long>, QuerydslPredicateExecutor<ReportNews> {
}