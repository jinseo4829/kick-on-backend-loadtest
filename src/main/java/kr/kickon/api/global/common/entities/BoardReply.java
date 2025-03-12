package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BoardReply")
@Getter
@Setter
public class BoardReply extends BaseEntity {
    @Column(nullable = false, length = 1000)
    private String contents;

    @ManyToOne
    @JoinColumn(name = "parent_board_reply_pk", foreignKey = @ForeignKey(name = "fk_board_reply_parent"))
    private BoardReply parentBoardReply;

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_board_reply_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "board_pk", foreignKey = @ForeignKey(name = "fk_board_reply_board"))
    private Board board;
}