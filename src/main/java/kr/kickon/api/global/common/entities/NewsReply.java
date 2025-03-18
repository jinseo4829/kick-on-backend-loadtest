package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "NewsReply")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class NewsReply extends BaseEntity {
    @Column(nullable = false, length = 1000)
    private String contents;

    @ManyToOne
    @JoinColumn(name = "parent_news_reply_pk", foreignKey = @ForeignKey(name = "fk_news_reply_parent"))
    private NewsReply parentNewsReply;

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_news_reply_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "news_pk", foreignKey = @ForeignKey(name = "fk_news_reply_news"))
    private News news;
}