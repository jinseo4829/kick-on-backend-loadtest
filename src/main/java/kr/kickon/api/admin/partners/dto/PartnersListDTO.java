package kr.kickon.api.admin.partners.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.ExampleConstants;
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

  @Schema(description = "파트너스 PK", example = ExampleConstants.userPk)
  private Long pk;

  @Schema(description = "파트너스 팀")
  private TeamDTO team;

  @Schema(description = "파트너스 닉네임", example = ExampleConstants.nickname)
  private String nickname;

  @Schema(description = "파트너스 이름", example = ExampleConstants.name)
  private String name;

  @Schema(description = "계약 시작일", example = ExampleConstants.startDatetime)
  private LocalDateTime contractStartDate;

  @Schema(description = "계약 종료일", example = ExampleConstants.endDatetime)
  private LocalDateTime contractEndDate;

  @Schema(description = "계약 상태", example = ExampleConstants.contractStatus)
  private ContractStatus contractStatus;

  public static kr.kickon.api.admin.partners.dto.PartnersListDTO fromEntity(Partners partners, TeamDTO teamDTO) {
    return PartnersListDTO.builder()
        .pk(partners.getPk())
        .team(teamDTO)
        .name(partners.getName())
        .nickname(partners.getUser().getNickname())
        .contractStartDate(partners.getContractStartDate())
        .contractEndDate(partners.getContractEndDate())
        .contractStatus(partners.getContractStatus())
        .build();
  }
}