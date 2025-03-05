package kr.kickon.api.domain.game;

import kr.kickon.api.global.common.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long>, QuerydslPredicateExecutor<Game> {
}