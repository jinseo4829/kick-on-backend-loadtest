package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.domain.game.dto.GameDTO;
import kr.kickon.api.global.common.enums.GameStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "Game")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Game extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String id;

    @Column
    private Long apiId;

    @Column
    private Integer homeScore;

    @Column
    private Integer awayScore;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus gameStatus;

    @Column(length = 50)
    private String round;

    @ManyToOne
    @JoinColumn(name = "home_team_pk", foreignKey = @ForeignKey(name = "fk_game_home_team"))
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_pk", foreignKey = @ForeignKey(name = "fk_game_away_team"))
    private Team awayTeam;

    @ManyToOne
    @JoinColumn(name = "actual_season_pk", foreignKey = @ForeignKey(name = "fk_game_actual_season"))
    private ActualSeason actualSeason;

    @Column
    private Integer homePenaltyScore;

    @Column
    private Integer awayPenaltyScore;

    @Override
    public String toString() {
        return "Game [apiId=" + apiId + ", homeScore=" + homeScore + ", awayScore=" + awayScore + ", startedAt=" + startedAt + ", gameStatus=" + gameStatus;
    }
}