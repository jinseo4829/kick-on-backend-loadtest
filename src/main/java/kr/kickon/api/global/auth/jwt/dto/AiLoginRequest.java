package kr.kickon.api.global.auth.jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiLoginRequest {

    @Schema(description = "AI 유저 이메일 (예: user@teamname.ai)", example = "user@arsenal.ai")
    private String email;

    @Schema(description = "고정된 AI 유저 패스워드", example = "kickon2025!")
    private String password;
}