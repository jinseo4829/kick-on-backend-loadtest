package kr.kickon.api.admin.partners.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;

@Data
@Schema(description = "파트너스 생성 요청 DTO")
public class CreatePartnersRequest {

  @NotNull
  @Schema(description = "파트너스 팀pk", example = ExampleConstants.teamPk)
  private Long teamPk;

  @NotNull
  @Schema(description = "파트너스 userPk", example = ExampleConstants.userPk)
  private Long userPk;

  @Schema(description = "연락할 파트너스 이메일", example = ExampleConstants.email)
  private String partnersEmail;

  @Schema(description = "파트너스 이름", example = ExampleConstants.name)
  private String name;

  @Schema(description = "sns 링크", example = ExampleConstants.snsUrl)
  private String snsUrl;
}