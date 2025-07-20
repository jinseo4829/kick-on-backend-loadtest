package kr.kickon.api.domain.gambleSeason;

import java.util.Optional;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GambleSeasonRepository extends JpaRepository<GambleSeason, Long>, QuerydslPredicateExecutor<GambleSeason> {
  Optional<GambleSeason> findByPkAndStatus(Long pk, DataStatus status);
}