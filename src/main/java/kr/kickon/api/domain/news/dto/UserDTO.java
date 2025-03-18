package kr.kickon.api.domain.news.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "뉴스 작성 유저 DTO")
public class UserDTO {
    @Schema(example = "1", description = "유저 고유 id값")
    private String id;
    @Schema(example = ExampleConstants.nickname, description = "닉네임")
    private String nickname;
    @Schema(example = ExampleConstants.profileImageUrl, description = "프로필 이미지")
    private String profileImageUrl;
}
