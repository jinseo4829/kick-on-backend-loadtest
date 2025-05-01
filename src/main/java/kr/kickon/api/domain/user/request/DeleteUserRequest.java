package kr.kickon.api.domain.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class DeleteUserRequest {
    @Schema(example = "서비스 품질 불만족", description = "탈퇴 사유")
    private String reason;
}