package kr.kickon.api.admin.partners.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.entities.Partners;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
@Schema(name = "AdminPartnersListDTO", description = "파트너스 리스트 조회 응답 DTO")
@AllArgsConstructor
public class PartnersListDTO {

  @Schema(description = "파트너스 PK", example = "1")
  private Long pk;



  @Schema(description = "파트너스 닉네임", example = "kickon_user")
  private String nickname;

  @Schema(description = "유저 이메일", example = "user@example.com")
  private String email;

  @Schema(description = "기타 정보", example = "etc 정보 예시")
  private String etc;

  public static kr.kickon.api.admin.partners.dto.PartnersListDTO fromEntity(Partners partners) {
    return kr.kickon.api.admin.partners.dto.PartnersListDTO.builder()
        .pk(partners.getPk())
        .etc(partners.getEtc())
        .build();
  }
}