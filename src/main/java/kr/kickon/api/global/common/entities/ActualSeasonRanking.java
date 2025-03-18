package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ActualSeasonRanking")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ActualSeasonRanking extends BaseEntity {
    @Column(nullable = false)
    private Integer rankOrder;

    @Column(nullable = false)
    private Integer gameNum;

    @Column(nullable = false)
    private Integer wins;

    @Column(nullable = false)
    private Integer draws;

    @Column(nullable = false)
    private Integer loses;

    @Column(nullable = false)
    private Integer wonScores;

    @Column(nullable = false)
    private Integer lostScores;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private int season;

    @ManyToOne
    @JoinColumn(name = "team_pk", foreignKey = @ForeignKey(name = "fk_actual_season_ranking_team"))
    private Team team;

    @ManyToOne
    @JoinColumn(name = "actual_season_pk", foreignKey = @ForeignKey(name = "fk_actual_season_ranking_actual_season"))
    private ActualSeason actualSeason;
}