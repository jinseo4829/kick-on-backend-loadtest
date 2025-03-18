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
public class NewsReplyKick extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_news_reply_kick_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "news_reply_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_news_reply_kick_news_reply"))
    private NewsReply newsReply;
}
