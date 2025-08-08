package kr.kickon.api.domain.newsKick;
import java.time.LocalDateTime;
import kr.kickon.api.global.common.entities.NewsKick;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsKickRepository extends JpaRepository<NewsKick, Long>, QuerydslPredicateExecutor<NewsKick> {
  Long countByNews_PkAndStatus(Long newsPk, DataStatus status);
}