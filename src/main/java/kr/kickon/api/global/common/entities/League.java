package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "League")
@Getter
@Setter
public class League extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 100)
    private String description;

    @Column(nullable = false)
    private Integer division;

    @ManyToOne
    @JoinColumn(name = "country_pk", foreignKey = @ForeignKey(name = "fk_league_country"))
    private Country country;
}