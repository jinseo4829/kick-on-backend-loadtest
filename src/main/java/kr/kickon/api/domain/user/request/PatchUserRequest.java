package kr.kickon.api.domain.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class PatchUserRequest {
    @NotNull
    @Length(min = 1, max = 10)
    @Schema(example = "민서짱9", description = "수정할 닉네임")
    private String nickname;
}