package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.GameStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Game")
@Getter
@Setter
public class Game extends BaseEntity {
    @Column
    private Integer homeScore;

    @Column
    private Integer awayScore;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus gameStatus;

    @ManyToOne
    @JoinColumn(name = "home_team_pk", foreignKey = @ForeignKey(name = "fk_game_home_team"))
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_pk", foreignKey = @ForeignKey(name = "fk_game_away_team"))
    private Team awayTeam;

    @ManyToOne
    @JoinColumn(name = "gamble_season_pk", foreignKey = @ForeignKey(name = "fk_game_gamble_season"))
    private GambleSeason gambleSeason;

    @ManyToOne
    @JoinColumn(name = "actual_season_pk", foreignKey = @ForeignKey(name = "fk_game_actual_season"))
    private ActualSeason actualSeason;
}