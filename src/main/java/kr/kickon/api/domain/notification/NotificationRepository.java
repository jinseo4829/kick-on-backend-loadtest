package kr.kickon.api.domain.notification;

import kr.kickon.api.global.common.entities.Notification;
import kr.kickon.api.global.common.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, QuerydslPredicateExecutor<Notification> {
    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);
    int countByReceiverAndIsReadIsFalse(User receiver);
}

