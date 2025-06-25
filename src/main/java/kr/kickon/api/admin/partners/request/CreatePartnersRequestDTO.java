package kr.kickon.api.admin.partners.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "파트너스 생성 요청 DTO")
public class CreatePartnersRequestDTO {

  @NotNull
  @Schema(description = "파트너스 팀pk", example = "1647")
  private Long teamPk;

  @NotNull
  @Schema(description = "파트너스 userPk", example = "38")
  private Long userPk;

  @Schema(description = "연락할 파트너스 이메일", example = "user@example.com")
  private String partnersEmail;

  @Schema(description = "파트너스 이름", example = "임민서")
  private String name;

  @Schema(description = "sns 링크", example = "https://www.youtube.com/@ncdinos")
  private String snsUrl;
}