package kr.kickon.api.global.common.entities;

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

    @Column()
    private String logoUrl;

    @Column(length = 3)
    private String code;

    @Column(nullable = false)
    private Long apiId;
}