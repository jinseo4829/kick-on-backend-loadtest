package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.GambleStatus;
import kr.kickon.api.global.common.enums.PredictedResult;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "UserGameGamble")
@Getter
@Setter
public class UserGameGamble extends BaseEntity {
    @Column(nullable = false)
    private Integer predictedHomeScore;

    @Column(nullable = false)
    private Integer predictedAwayScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PredictedResult predictedResult;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GambleStatus gambleStatus;

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_user_game_gamble_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_pk", foreignKey = @ForeignKey(name = "fk_user_game_gamble_game"))
    private Game game;

    @ManyToOne
    @JoinColumn(name = "supporting_team_pk", foreignKey = @ForeignKey(name = "fk_user_game_gamble_team"))
    private Team supportingTeam;
}

