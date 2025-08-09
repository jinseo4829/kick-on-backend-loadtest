package kr.kickon.api.domain.teamReporter;
import kr.kickon.api.global.common.entities.ReportNews;
import kr.kickon.api.global.common.entities.TeamReporter;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamReporterRepository extends JpaRepository<TeamReporter, Long>, QuerydslPredicateExecutor<TeamReporter> {
    TeamReporter findByUserAndStatus(User user, DataStatus status);
    TeamReporter findByUserIdAndStatus(String id, DataStatus status);
}