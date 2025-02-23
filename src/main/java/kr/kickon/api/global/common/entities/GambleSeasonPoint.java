package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "GambleSeasonPoint")
@Getter
@Setter
public class GambleSeasonPoint extends BaseEntity {
    @Column(nullable = false)
    private Integer averagePoints;

    @ManyToOne
    @JoinColumn(name = "gamble_season_pk", foreignKey = @ForeignKey(name = "fk_gamble_season_point_season"))
    private GambleSeason gambleSeason;

    @ManyToOne
    @JoinColumn(name = "team_pk", foreignKey = @ForeignKey(name = "fk_gamble_season_point_team"))
    private Team team;

    @ManyToOne
    @JoinColumn(name = "game_pk", foreignKey = @ForeignKey(name = "fk_gamble_season_point_game"))
    private Game game;
}