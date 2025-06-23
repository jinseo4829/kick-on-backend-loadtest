package kr.kickon.api.domain.partners;

import kr.kickon.api.global.common.entities.Partners;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnersRepository extends JpaRepository<Partners, Long>, QuerydslPredicateExecutor<Partners>{
  boolean existsByUserPkAndStatus(Long userPk, DataStatus status);

}