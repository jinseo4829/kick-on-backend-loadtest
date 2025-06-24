package kr.kickon.api.global.common.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.kickon.api.global.common.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@Table(name = "Partners")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Partners extends BaseEntity{
  @Column(length = 10)
  private String name;

  @Column(nullable = false, length = 256)
  private String partnersEmail;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private ContractStatus contractStatus = ContractStatus.BEFORE_CONTACT;

  @Column()
  private LocalDateTime contractStartDate;

  @Column()
  private LocalDateTime contractEndDate;

  @Schema(description = "sns url", example = "https://www.youtube.com/channel/UC1234567890abcdef")
  @Column(columnDefinition = "TEXT")
  private String snsUrl;

  @ManyToOne
  @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_partners_user"))
  private User user;
}


