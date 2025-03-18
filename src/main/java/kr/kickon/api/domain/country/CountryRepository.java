package kr.kickon.api.domain.country;

import kr.kickon.api.global.common.entities.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long>, QuerydslPredicateExecutor<Country> {
}