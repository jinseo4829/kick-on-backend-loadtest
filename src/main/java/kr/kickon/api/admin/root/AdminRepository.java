package kr.kickon.api.admin.root;

import kr.kickon.api.global.common.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>, QuerydslPredicateExecutor<Admin> {
}