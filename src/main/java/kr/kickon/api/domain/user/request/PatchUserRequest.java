package kr.kickon.api.domain.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
public class PatchUserRequest {
    @Length(min = 1, max = 10,message = "닉네임은 10자 이하로 작성해주세요.")
    @Schema(example = "민서짱9", description = "수정할 닉네임, 수정 필요 없으면 안보내도 됨")
    private String nickname;

    @Schema(example = "[1, 2]", description = "응원하는 팀 pk 리스트, 수정 필요 없으면 안보내도 됨. 앞에있는 순서대로 우선순위 설정")
    @Size(max = 3, message = "최대 3개까지 설정 가능합니다.")
    private List<Long> teams;

    @Schema(example = ExampleConstants.profileImageUrl, description = "프로필 사진 이미지 url, 수정 필요 없으면 안보내도 됨")
    private String profileImageUrl;
}