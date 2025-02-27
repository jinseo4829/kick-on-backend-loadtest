package kr.kickon.api.global.common.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "Team")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Team extends BaseEntity {
    @Column(length = 50)
    private String nameKr;

    @Column(nullable = false, length = 100)
    private String nameEn;

    @Column(nullable = false)
    private String logoUrl;

    @Column(nullable = false, length = 3)
    private String code;

    @Column(nullable = false)
    private Long apiId;
}