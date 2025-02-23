package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ActualSeasonTeam")
@Getter
@Setter
public class ActualSeasonTeam extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "actual_season_pk", foreignKey = @ForeignKey(name = "fk_actual_season_team_season"))
    private ActualSeason actualSeason;

    @ManyToOne
    @JoinColumn(name = "team_pk", foreignKey = @ForeignKey(name = "fk_actual_season_team_team"))
    private Team team;
}