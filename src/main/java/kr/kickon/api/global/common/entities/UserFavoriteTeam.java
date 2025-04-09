package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "UserFavoriteTeam")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class UserFavoriteTeam extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_user_favorite_team_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_pk", foreignKey = @ForeignKey(name = "fk_user_favorite_team_team"))
    private Team team;
}