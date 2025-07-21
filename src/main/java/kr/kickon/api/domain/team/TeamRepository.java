package kr.kickon.api.domain.team;

import java.util.Optional;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>, QuerydslPredicateExecutor<Team> {
  Optional<Team> findByPkAndStatus(Long pk, DataStatus status);
}