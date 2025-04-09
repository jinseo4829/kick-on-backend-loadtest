package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.ReportStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ReportBoard")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ReportBoard extends BaseEntity {
    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus reportStatus;

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_report_board_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "reported_board_pk", foreignKey = @ForeignKey(name = "fk_report_board_board"))
    private Board reportedBoard;
}