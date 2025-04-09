package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "NewsKick")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class NewsKick extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_news_kick_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "news_pk", foreignKey = @ForeignKey(name = "fk_news_kick_news"))
    private News news;
}