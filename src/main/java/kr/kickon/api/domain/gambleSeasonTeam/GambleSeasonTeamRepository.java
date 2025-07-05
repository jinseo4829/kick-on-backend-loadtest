package kr.kickon.api.domain.gambleSeasonTeam;

import java.util.List;
import kr.kickon.api.global.common.entities.GambleSeasonTeam;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GambleSeasonTeamRepository extends JpaRepository<GambleSeasonTeam, Long>, QuerydslPredicateExecutor<GambleSeasonTeam> {
  List<GambleSeasonTeam> findAllByGambleSeason_PkAndStatus(Long gambleSeasonPk, DataStatus status);

}