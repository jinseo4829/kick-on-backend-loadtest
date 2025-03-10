package kr.kickon.api.domain.gambleSeasonRanking;

import kr.kickon.api.global.common.entities.GambleSeasonRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GambleSeasonRankingRepository extends JpaRepository<GambleSeasonRanking, Long>, QuerydslPredicateExecutor<GambleSeasonRanking> {
}