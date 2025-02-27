package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.LeagueType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "League")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class League extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String enName;

    @Column(nullable = false, length = 50)
    private String krName;

    @Column(length = 100)
    private String description;

    @Column(nullable = false)
    private Integer division;

    @Column(nullable = false)
    private Long apiId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LeagueType type;

    @Column()
    private String logoUrl;

    @ManyToOne
    @JoinColumn(name = "country_pk", foreignKey = @ForeignKey(name = "fk_league_country"))
    private Country country;
}