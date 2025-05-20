package kr.kickon.api.admin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    @Schema(description = "accessToken", example = "salkdjflsakdjflsakjdf")
    private String accessToken;
    @Schema(description = "refreshToken", example = "salkdjflsakdjflsakjdf")
    private String refreshToken;
}
