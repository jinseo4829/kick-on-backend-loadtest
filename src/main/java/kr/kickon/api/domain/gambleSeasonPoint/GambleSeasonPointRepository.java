package kr.kickon.api.domain.gambleSeasonPoint;

import kr.kickon.api.global.common.entities.GambleSeasonPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GambleSeasonPointRepository extends JpaRepository<GambleSeasonPoint, Long>, QuerydslPredicateExecutor<GambleSeasonPoint> {
}