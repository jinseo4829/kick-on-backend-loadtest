package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ActualSeason")
@Getter
@Setter
public class ActualSeason extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 100)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperatingStatus operatingStatus;

    @ManyToOne
    @JoinColumn(name = "league_pk", foreignKey = @ForeignKey(name = "fk_actual_season_league"))
    private League league;
}
