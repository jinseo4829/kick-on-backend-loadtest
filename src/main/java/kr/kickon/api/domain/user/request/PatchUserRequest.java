package kr.kickon.api.domain.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
public class PatchUserRequest {
    @NotNull(message = "닉네임은 필수값입니다.")
    @Length(min = 1, max = 10,message = "닉네임은 10자 이하로 작성해주세요.")
    @Schema(example = "민서짱9", description = "수정할 닉네임")
    private String nickname;

    @Positive(message = "team PK값은 양수입니다.")
    @Schema(example = "1", description = "응원하는 팀 pk, 수정 필요 없으면 안보내도 됨")
    private Long team;

    @Schema(example = ExampleConstants.profileImageUrl, description = "프로필 사진 이미지 url")
    private String ProfileImageUrl;
}