package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BoardViewHistory")
@Getter
@Setter
public class BoardViewHistory extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_board_view_history_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "board_pk", foreignKey = @ForeignKey(name = "fk_board_view_history_board"))
    private Board board;
}