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

    @Column(nullable = false)
    private String logoUrl;

    @Column(nullable = false, length = 3)
    private String code;

    @Column(nullable = false)
    private long apiId;
}