package kr.kickon.api.domain.news;

import kr.kickon.api.domain.news.dto.NewsListDTO;
import kr.kickon.api.global.common.entities.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface NewsRepository extends JpaRepository<News, Long>, QuerydslPredicateExecutor<News> {
}