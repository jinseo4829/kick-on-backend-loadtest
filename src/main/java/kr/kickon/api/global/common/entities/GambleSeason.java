package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "GambleSeason")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class GambleSeason extends BaseEntity {
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
    @JoinColumn(name = "actual_season_pk", foreignKey = @ForeignKey(name = "fk_gamble_season_actual_season"))
    private ActualSeason actualSeason;
}