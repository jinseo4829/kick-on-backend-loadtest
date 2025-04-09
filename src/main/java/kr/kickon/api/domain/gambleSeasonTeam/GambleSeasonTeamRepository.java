package kr.kickon.api.domain.gambleSeasonTeam;

import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.GambleSeasonTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GambleSeasonTeamRepository extends JpaRepository<GambleSeasonTeam, Long>, QuerydslPredicateExecutor<GambleSeasonTeam> {
}