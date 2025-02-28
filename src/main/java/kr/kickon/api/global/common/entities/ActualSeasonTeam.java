package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ActualSeasonTeam")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ActualSeasonTeam extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "actual_season_pk", foreignKey = @ForeignKey(name = "fk_actual_season_team_season"))
    private ActualSeason actualSeason;

    @ManyToOne
    @JoinColumn(name = "team_pk", foreignKey = @ForeignKey(name = "fk_actual_season_team_team"))
    private Team team;
}