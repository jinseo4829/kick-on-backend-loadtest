package kr.kickon.api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

@Getter
@Setter
@Schema(name = "GetUserMeDTO", description = "사용자 고유 ID")
public class GetUserMeDTO {
    @Schema(description = "사용자 고유 ID", example = "sdfjkl2-sldkjf2-wkjl")
    private String id;

    @Schema(description = "닉네임", example = "민서짱9")
    private String nickname;

    @Schema(description = "사용자 프로필 이미지", example = "https://naver.me/image.png")
    private String profileImageUrl;

    @Schema(description = "팀 로고 url", example = "https://naver.me/image.png")
    private String teamLogoUrl;

    public GetUserMeDTO(User user,Team team) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.teamLogoUrl = team.getLogoUrl();
    }

    public GetUserMeDTO(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.teamLogoUrl = null;
    }
}
