package kr.kickon.api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "기본 유저 정보 DTO")
public class BaseUserDTO {
    @Schema(description = "사용자 고유 ID", example = ExampleConstants.id)
    private String id;

    @Schema(description = "닉네임", example = "민서짱9")
    private String nickname;

    @Schema(description = "사용자 프로필 이미지", example = "https://naver.me/image.png")
    private String profileImageUrl;

    @Schema(description = "구단기자 여부", example = "false")
    private Boolean isReporter=false;

    public BaseUserDTO(User user) {
        id = user.getId();
        nickname = user.getNickname();
        profileImageUrl = user.getProfileImageUrl();
    }
}
