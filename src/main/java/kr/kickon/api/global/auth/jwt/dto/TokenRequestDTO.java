package kr.kickon.api.global.auth.jwt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TokenRequestDTO {
    @NotNull
    String refreshToken;
}
