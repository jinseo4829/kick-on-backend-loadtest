package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "Admin")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Admin extends BaseEntity {
    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String password;
}
