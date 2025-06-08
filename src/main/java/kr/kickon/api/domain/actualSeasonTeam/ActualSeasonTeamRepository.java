package kr.kickon.api.domain.actualSeasonTeam;

import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActualSeasonTeamRepository extends JpaRepository<ActualSeasonTeam, Long>, QuerydslPredicateExecutor<ActualSeasonTeam> {
    boolean existsByActualSeasonAndTeam_Pk(ActualSeason actualSeason, Long teamPk);
}