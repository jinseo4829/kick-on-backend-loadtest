package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kotlin.reflect.KType;
import kr.kickon.api.global.common.converters.BooleanConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "Board")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Board extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contents;

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_board_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_pk", foreignKey = @ForeignKey(name = "fk_board_team"))
    private Team team;

    @Column(nullable = false)
    @Convert(converter = BooleanConverter.class)
    private Boolean hasImage = false;

    @Column()
    @Convert(converter = BooleanConverter.class)
    private Boolean isPinned = false;
}