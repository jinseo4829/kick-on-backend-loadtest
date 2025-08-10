package kr.kickon.api.global.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import kr.kickon.api.global.common.enums.UsedInType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "EmbeddedLink")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EmbeddedLink extends BaseEntity {

  @Column(length = 512, nullable = false, name = "url")
  private String url;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UsedInType usedIn;

  @Column()
  private Long referencePk;
}
