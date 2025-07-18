package kr.kickon.api.admin.partners.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.kickon.api.admin.user.dto.UserListDTO;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.Partners;
import kr.kickon.api.global.common.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
@AllArgsConstructor
@Schema(name = "AdminPartnersDetailDTO", description = "파트너스 상세 조회 응답 DTO")
public class PartnersDetailDTO {

  @Schema(description = "파트너스 PK", example = ExampleConstants.userPk)
  private Long pk;

  @Schema(description = "파트너스 팀")
  private TeamDTO team;

  @Schema(description = "파트너스 user 정보")
  private UserListDTO user;

  @Schema(description = "연락할 파트너스 이메일", example = ExampleConstants.email)
  private String partnersEmail;

  @Schema(description = "파트너스 이름", example = ExampleConstants.name)
  private String name;

  @Schema(description = "sns 링크", example = ExampleConstants.snsUrl)
  private String snsUrl;

  @Schema(description = "계약 시작일", example = ExampleConstants.startDatetime)
  private LocalDateTime contractStartDate;

  @Schema(description = "계약 종료일", example = ExampleConstants.endDatetime)
  private LocalDateTime contractEndDate;

  @Schema(description = "계약 상태", example = ExampleConstants.contractStatus)
  private ContractStatus contractStatus;

  @Schema(description = "기타 정보", example = ExampleConstants.etc)
  private String etc;

  public static PartnersDetailDTO fromEntity(Partners partners, UserListDTO userDto, TeamDTO teamDTO) {
    return PartnersDetailDTO.builder()
        .pk(partners.getPk())
        .name(partners.getName())
        .partnersEmail(partners.getPartnersEmail())
        .snsUrl(partners.getSnsUrl())
        .contractStartDate(partners.getContractStartDate())
        .contractEndDate(partners.getContractEndDate())
        .contractStatus(partners.getContractStatus())
        .etc(partners.getEtc())
        .user(userDto)
        .team(teamDTO)
        .build();
  }

}