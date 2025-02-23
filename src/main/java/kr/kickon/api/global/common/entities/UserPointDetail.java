package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.PointStatus;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "UserPointDetail")
@Getter
@Setter
public class UserPointDetail extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointStatus pointStatus;

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_user_point_detail_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "parent_user_point_detail_pk", foreignKey = @ForeignKey(name = "fk_user_point_detail_parent"))
    private UserPointDetail parentUserPointDetail;

    @ManyToOne
    @JoinColumn(name = "user_point_event_pk", foreignKey = @ForeignKey(name = "fk_user_point_detail_event"))
    private UserPointEvent userPointEvent;
}