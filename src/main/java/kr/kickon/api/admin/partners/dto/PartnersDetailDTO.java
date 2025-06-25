package kr.kickon.api.admin.partners.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.kickon.api.admin.user.dto.UserListDTO;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
@AllArgsConstructor
@Schema(name = "AdminPartnersDetailDTO", description = "유저 상세 조회 응답 DTO")
public class PartnersDetailDTO {

  @Schema(description = "파트너스 PK", example = "1")
  private Long pk;

  @Schema(description = "파트너스 팀")
  private TeamDTO team;

  @Schema(description = "파트너스 user 정보")
  private UserListDTO user;

  @Schema(description = "연락할 파트너스 이메일", example = "user@example.com")
  private String partnersEmail;

  @Schema(description = "파트너스 이름", example = "임민서")
  private String name;

  @Schema(description = "sns 링크", example = "https://www.youtube.com/@ncdinos")
  private String snsUrl;

  @Schema(description = "계약 시작일", example = "2024-01-01T12:00:00")
  private LocalDateTime contractStartDate;

  @Schema(description = "계약 종료일", example = "2024-01-30T12:00:00")
  private LocalDateTime contractEndDate;

  @Schema(description = "계약 상태", example = "BEFORE_CONTACT")
  private ContractStatus contractStatus;

  @Schema(description = "기타 정보", example = "etc 정보 예시")
  private String etc;
}