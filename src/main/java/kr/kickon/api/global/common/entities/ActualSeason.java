package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ActualSeason")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ActualSeason extends BaseEntity {
    @Column(length = 50)
    private String title;

    @Column(length = 100)
    private String description;

    @Column(nullable = false)
    private LocalDate startedAt;

    @Column
    private LocalDate finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperatingStatus operatingStatus;

    @Column
    private int year;

    @ManyToOne
    @JoinColumn(name = "league_pk", foreignKey = @ForeignKey(name = "fk_actual_season_league"))
    private League league;
}
