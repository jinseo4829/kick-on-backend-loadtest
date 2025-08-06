package kr.kickon.api.domain.embeddedLink;

import kr.kickon.api.global.common.entities.BoardViewHistory;
import kr.kickon.api.global.common.entities.EmbeddedLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbeddedLinkRepository extends JpaRepository<EmbeddedLink, Long>, QuerydslPredicateExecutor<EmbeddedLink> {

}