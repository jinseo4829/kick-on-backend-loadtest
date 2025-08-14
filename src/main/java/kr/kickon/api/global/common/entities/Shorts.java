package kr.kickon.api.global.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import kr.kickon.api.global.common.enums.ShortsType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "Shorts")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Shorts extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ShortsType type;

  @Column()
  private Long referencePk;
}
