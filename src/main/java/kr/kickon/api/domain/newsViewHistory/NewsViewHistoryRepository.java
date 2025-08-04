package kr.kickon.api.domain.newsViewHistory;
import java.time.LocalDateTime;
import kr.kickon.api.global.common.entities.NewsViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsViewHistoryRepository extends JpaRepository<NewsViewHistory, Long>, QuerydslPredicateExecutor<NewsViewHistory> {
  Long countByNews_Pk(Long newsPk);

  Long countByNewsPkAndCreatedAtAfter(Long newsPk, LocalDateTime createdAtAfter);

}