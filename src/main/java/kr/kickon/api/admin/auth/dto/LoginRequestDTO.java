package kr.kickon.api.admin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class LoginRequestDTO {
    @Schema(description = "이메일", example = "admin@kick-on.kr")
    private String email;

    @Schema(description = "비밀번호", example = "kickon2025!")
    private String password;
}
