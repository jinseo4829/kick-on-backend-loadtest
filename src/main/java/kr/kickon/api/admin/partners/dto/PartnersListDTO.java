package kr.kickon.api.admin.partners.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.entities.Partners;
import kr.kickon.api.global.common.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Data
@Builder(toBuilder = true)
@Schema(name = "AdminPartnersListDTO", description = "파트너스 리스트 조회 응답 DTO")
@AllArgsConstructor
public class PartnersListDTO {

  @Schema(description = "파트너스 PK", example = "1")
  private Long pk;

  @Schema(description = "파트너스가 응원하는 팀 리스트")
  private List<TeamDTO> favoriteTeams;

  @Schema(description = "파트너스 닉네임", example = "kickon_user")
  private String nickname;

  @Schema(description = "파트너스 이름", example = "임민서")
  private String name;

  @Schema(description = "계약 시작일", example = "2024-01-01T12:00:00")
  private LocalDateTime contractStartDate;

  @Schema(description = "계약 종료일", example = "2024-01-30T12:00:00")
  private LocalDateTime contractEndDate;

  @Schema(description = "계약 상태", example = "BEFORE_CONTACT")
  private ContractStatus contractStatus;

  public static kr.kickon.api.admin.partners.dto.PartnersListDTO fromEntity(Partners partners) {
    return PartnersListDTO.builder()
        .pk(partners.getPk())
        .name(partners.getName())
        .nickname(partners.getUser().getNickname())
        .contractStartDate(partners.getContractStartDate())
        .contractEndDate(partners.getContractEndDate())
        .contractStatus(partners.getContractStatus())
        .build();
  }
}