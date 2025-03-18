package kr.kickon.api.domain.newsKick;
import kr.kickon.api.global.common.entities.NewsKick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsKickRepository extends JpaRepository<NewsKick, Long>, QuerydslPredicateExecutor<NewsKick> {
}