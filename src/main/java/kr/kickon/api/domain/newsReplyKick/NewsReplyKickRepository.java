package kr.kickon.api.domain.newsReplyKick;
import kr.kickon.api.global.common.entities.BoardReplyKick;
import kr.kickon.api.global.common.entities.NewsReplyKick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsReplyKickRepository extends JpaRepository<NewsReplyKick, Long>, QuerydslPredicateExecutor<NewsReplyKick> {
}