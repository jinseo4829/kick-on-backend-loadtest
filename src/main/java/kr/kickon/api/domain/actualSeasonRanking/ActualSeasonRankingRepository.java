package kr.kickon.api.domain.actualSeasonRanking;

import kr.kickon.api.global.common.entities.ActualSeasonRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ActualSeasonRankingRepository extends JpaRepository<ActualSeasonRanking, Long>, QuerydslPredicateExecutor<ActualSeasonRanking> {
}