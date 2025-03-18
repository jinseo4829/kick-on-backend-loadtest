package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.PointCategory;
import kr.kickon.api.global.common.enums.PointStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "UserPointEvent")
@Getter
@Setter
public class UserPointEvent extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointStatus pointStatus;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private Integer point = 0;

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_user_point_event_user"))
    private User user;
}

