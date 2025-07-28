package kr.kickon.api.domain.actualSeason;

import java.util.Optional;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ActualSeasonRepository extends JpaRepository<ActualSeason, Long>, QuerydslPredicateExecutor<ActualSeason> {
  Optional<ActualSeason> findByPkAndStatus(Long pk, DataStatus status);
}