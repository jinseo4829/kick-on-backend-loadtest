package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "GambleSeasonTeam")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class GambleSeasonTeam extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "gamble_season_pk", foreignKey = @ForeignKey(name = "fk_gamble_season_team_season"))
    private GambleSeason gambleSeason;

    @ManyToOne
    @JoinColumn(name = "team_pk", foreignKey = @ForeignKey(name = "fk_gamble_season_team_team"))
    private Team team;
}