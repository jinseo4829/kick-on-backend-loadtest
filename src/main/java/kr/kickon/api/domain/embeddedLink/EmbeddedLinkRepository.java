package kr.kickon.api.domain.embeddedLink;

import java.util.List;
import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.entities.BoardViewHistory;
import kr.kickon.api.global.common.entities.EmbeddedLink;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.UsedInType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbeddedLinkRepository extends JpaRepository<EmbeddedLink, Long>, QuerydslPredicateExecutor<EmbeddedLink> {
  List<EmbeddedLink> findByUsedInEqualsAndReferencePkEqualsAndStatus(UsedInType usedIn, Long referencePk, DataStatus status);

}