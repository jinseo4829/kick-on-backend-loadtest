package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "NewsViewHistory")
@Getter
@Setter
public class NewsViewHistory extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_news_view_history_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "news_pk", foreignKey = @ForeignKey(name = "fk_news_view_history_news"))
    private News news;
}