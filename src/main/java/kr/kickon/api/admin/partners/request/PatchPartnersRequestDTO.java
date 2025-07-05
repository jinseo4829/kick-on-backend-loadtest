package kr.kickon.api.admin.partners.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Schema(name = "PatchPartnersRequestDTO", description = "파트너스 수정 요청 DTO")
public class PatchPartnersRequestDTO {

  @Schema(description = "파트너스 팀 PK", example = "1647")
  private Long teamPk;

  @Schema(description = "파트너스 userPk", example = "38")
  private Long userPk;

  @Schema(description = "연락할 파트너스 이메일", example = "user@example.com")
  private String partnersEmail;

  @Schema(description = "파트너스 이름", example = "임민서")
  private String name;

  @Schema(description = "sns 링크", example = "https://www.youtube.com/@ncdinos")
  private String snsUrl;

  @Schema(description = "계약 시작일", example = "2024-01-01T12:00:00")
  private String contractStartDate;

  @Schema(description = "계약 종료일", example = "2024-01-30T12:00:00")
  private String contractEndDate;

  @Schema(description = "계약 상태", example = "BEFORE_CONTACT")
  private String contractStatus;

  @Schema(description = "기타 정보", example = "etc 정보 예시")
  private String etc;
}
