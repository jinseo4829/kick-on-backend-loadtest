package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BoardKick")
@Getter
@Setter
public class BoardKick extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_board_kick_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "board_pk", foreignKey = @ForeignKey(name = "fk_board_kick_board"))
    private Board board;
}