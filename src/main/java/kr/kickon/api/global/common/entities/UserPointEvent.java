package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.PointCategory;
import kr.kickon.api.global.common.enums.PointStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "UserPointEvent")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class UserPointEvent extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointStatus pointStatus;

    @Column()
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private Integer point = 0;

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_user_point_event_user"))
    private User user;
}

