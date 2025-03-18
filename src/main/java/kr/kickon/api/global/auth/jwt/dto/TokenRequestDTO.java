package kr.kickon.api.global.auth.jwt.dto;

import lombok.Getter;
import software.amazon.awssdk.annotations.NotNull;

@Getter
public class TokenRequestDTO {
    @NotNull
    String refreshToken;
}
