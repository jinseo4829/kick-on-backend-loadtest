package kr.kickon.api.global.common.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Team")
@Getter
@Setter
public class Team extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String nameKr;

    @Column(nullable = false, length = 100)
    private String nameEn;

    @ManyToOne
    @JoinColumn(name = "league_pk", foreignKey = @ForeignKey(name = "fk_team_league"))
    private League league;

    @Schema(description = "응원팀 로고 image url")
    @Column(nullable = false)
    private String logoUrl;
}