package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Country")
@Getter
@Setter
public class Country extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String nameKr;

    @Column(nullable = false, length = 100)
    private String nameEn;

    @Column(nullable = false)
    private String flagUrl;

    @Column(nullable = false, length = 10)
    private String code;
}