package kr.kickon.api.domain.newsReply;
import kr.kickon.api.global.common.entities.NewsReply;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsReplyRepository extends JpaRepository<NewsReply, Long>, QuerydslPredicateExecutor<NewsReply> {
  Long countByNews_PkAndStatus(Long newsPk, DataStatus status);
}