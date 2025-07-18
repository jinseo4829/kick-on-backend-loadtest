package kr.kickon.api.admin.partners.request;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;

@Data
@Schema(name = "PatchPartnersRequestDTO", description = "파트너스 수정 요청 DTO")
public class UpdatePartnersRequest {

  @Schema(description = "파트너스 팀 PK", example = ExampleConstants.teamPk)
  private Long teamPk;

  @Schema(description = "파트너스 userPk", example = ExampleConstants.userPk)
  private Long userPk;

  @Schema(description = "연락할 파트너스 이메일", example = ExampleConstants.email)
  private String partnersEmail;

  @Schema(description = "파트너스 이름", example = ExampleConstants.name)
  private String name;

  @Schema(description = "sns 링크", example = ExampleConstants.snsUrl)
  private String snsUrl;

  @Schema(description = "계약 시작일", example = ExampleConstants.startDatetime)
  private String contractStartDate;

  @Schema(description = "계약 종료일", example = ExampleConstants.endDatetime)
  private String contractEndDate;

  @Schema(description = "계약 상태", example = ExampleConstants.contractStatus)
  private String contractStatus;

  @Schema(description = "기타 정보", example = ExampleConstants.etc)
  private String etc;
}
