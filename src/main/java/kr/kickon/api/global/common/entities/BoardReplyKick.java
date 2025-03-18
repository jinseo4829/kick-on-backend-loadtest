package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class BoardReplyKick extends BaseEntity {

    @ManyToOne()
    @JoinColumn(name = "user_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_board_reply_kick_user"))
    private User user;

    @ManyToOne()
    @JoinColumn(name = "board_reply_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_board_reply_kick_board_reply"))
    private BoardReply boardReply;
}
