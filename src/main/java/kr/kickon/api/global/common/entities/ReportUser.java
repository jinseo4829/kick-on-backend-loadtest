package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.ReportStatus;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ReportUser")
@Getter
@Setter
public class ReportUser extends BaseEntity {
    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus reportStatus;

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_report_user_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "reported_user_pk", foreignKey = @ForeignKey(name = "fk_report_user_reported_user"))
    private User reportedUser;
}

