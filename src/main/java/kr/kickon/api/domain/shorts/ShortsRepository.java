package kr.kickon.api.domain.shorts;

import kr.kickon.api.global.common.entities.Shorts;
import kr.kickon.api.global.common.enums.ShortsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortsRepository extends JpaRepository<Shorts, Long>, QuerydslPredicateExecutor<Shorts> {
  Shorts findByReferencePkAndType(Long referncePk, ShortsType shortsType);
}
