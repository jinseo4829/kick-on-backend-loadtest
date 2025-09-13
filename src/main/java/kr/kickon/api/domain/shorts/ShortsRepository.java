package kr.kickon.api.domain.shorts;

import kr.kickon.api.global.common.entities.Shorts;
import kr.kickon.api.global.common.enums.ShortsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortsRepository extends JpaRepository<Shorts, Long>, QuerydslPredicateExecutor<Shorts> {
  Shorts findByReferencePkAndType(Long referncePk, ShortsType shortsType);

    @Query(value = """
        (SELECT s.id AS shorts_id, COUNT(vh.id) AS view_count
         FROM shorts s
         JOIN aws_file_reference afr ON s.aws_file_reference_id = afr.id
         JOIN board_view_history vh ON vh.board_id = s.board_id
         WHERE s.type = 'AWS_FILE' AND afr.used_in = 'BOARD'
         GROUP BY s.id)
         
        UNION ALL
         
        (SELECT s.id AS shorts_id, COUNT(vh.id) AS view_count
         FROM shorts s
         JOIN aws_file_reference afr ON s.aws_file_reference_id = afr.id
         JOIN news_view_history vh ON vh.news_id = s.news_id
         WHERE s.type = 'AWS_FILE' AND afr.used_in = 'NEWS'
         GROUP BY s.id)
         
        UNION ALL
         
        (SELECT s.id AS shorts_id, COUNT(vh.id) AS view_count
         FROM shorts s
         JOIN embedded_link el ON s.embedded_link_id = el.id
         JOIN board_view_history vh ON vh.board_id = s.board_id
         WHERE s.type = 'EMBEDDED_LINK' AND el.used_in = 'BOARD'
         GROUP BY s.id)
         
        UNION ALL
         
        (SELECT s.id AS shorts_id, COUNT(vh.id) AS view_count
         FROM shorts s
         JOIN embedded_link el ON s.embedded_link_id = el.id
         JOIN news_view_history vh ON vh.news_id = s.news_id
         WHERE s.type = 'EMBEDDED_LINK' AND el.used_in = 'NEWS'
         GROUP BY s.id)
        """, nativeQuery = true)
    List<Object[]> findShortsWithViewCount();
}
