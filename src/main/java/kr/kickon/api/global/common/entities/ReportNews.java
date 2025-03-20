package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.ReportStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ReportNews")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReportNews extends BaseEntity {
    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus reportStatus;

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_report_news_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "reported_board_pk", foreignKey = @ForeignKey(name = "fk_report_news_news"))
    private News reportedNews;
}