package kr.kickon.api.domain.league;

import kr.kickon.api.global.common.entities.ActualSeasonRanking;
import kr.kickon.api.global.common.entities.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long>, QuerydslPredicateExecutor<League> {
}