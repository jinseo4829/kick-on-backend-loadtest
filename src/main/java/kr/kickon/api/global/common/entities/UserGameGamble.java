package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.GambleStatus;
import kr.kickon.api.global.common.enums.PredictedResult;
import kr.kickon.api.global.common.enums.UserAccountStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "UserGameGamble")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
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
    @Builder.Default
    @ColumnDefault(GambleStatus.VALUE.COMPLETED)
    private GambleStatus gambleStatus = GambleStatus.COMPLETED;

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_user_game_gamble_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_pk", foreignKey = @ForeignKey(name = "fk_user_game_gamble_game"))
    private Game game;

    @ManyToOne
    @JoinColumn(name = "supporting_team_pk", foreignKey = @ForeignKey(name = "fk_user_game_gamble_team"))
    private Team supportingTeam;

    @Override
    public String toString() {
        return "UserGameGamble [predictedHomeScore=" + predictedHomeScore + ", predictedAwayScore=" + predictedAwayScore + ", predictedResult=" + predictedResult + ", user=" + user.getNickname() + ", supprorting = " + supportingTeam.getNameKr()+"]";
    }
}

