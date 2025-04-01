package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.converters.BooleanConverter;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "EventBoard")
@Getter
@Setter
public class EventBoard extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String embeddedUrl;

    @Column
    private Integer orderNum;

    @Column(nullable = false)
    @Convert(converter = BooleanConverter.class)
    private Boolean isDisplayed;
}