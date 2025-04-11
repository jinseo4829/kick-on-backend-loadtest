package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "GambleSeasonRanking")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class GambleSeasonRanking extends BaseEntity {
    @Column(nullable = false)
    private Integer rankOrder;

    @Column(nullable = false)
    private Integer gameNum;

    @Column(nullable = false)
    private Integer points;

    @ManyToOne
    @JoinColumn(name = "gamble_season_pk", foreignKey = @ForeignKey(name = "fk_gamble_season_ranking_season"))
    private GambleSeason gambleSeason;

    @ManyToOne
    @JoinColumn(name = "team_pk", foreignKey = @ForeignKey(name = "fk_gamble_season_ranking_team"))
    private Team team;
}