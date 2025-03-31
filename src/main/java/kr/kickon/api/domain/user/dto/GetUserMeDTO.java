package kr.kickon.api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.enums.ProviderType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Schema(name = "GetUserMeDTO", description = "사용자 고유 ID")
public class GetUserMeDTO {
    @Schema(description = "사용자 고유 ID", example = "sdfjkl2-sldkjf2-wkjl")
    private String id;

    @Schema(description = "닉네임", example = "민서짱9")
    private String nickname;

    @Schema(description = "이메일", example = "limms1217@naver.com")
    private String email;

    @Schema(description = "사용자 프로필 이미지", example = "https://naver.me/image.png")
    private String profileImageUrl;

    @Schema(description = "소셜 로그인 provider", example = "KAKAO")
    private ProviderType providerType;

    @Schema(description = "팀 pk", example = "1")
    private Long teamPk;

    @Schema(description = "팀 로고 url", example = "https://naver.me/image.png")
    private String teamLogoUrl;

    @Schema(description = "팀 이름", example = ExampleConstants.teamKrName)
    private String teamName;

    @Schema(description = "리그 로고 url", example = ExampleConstants.teamLogoUrl)
    private String leagueLogoUrl;

    @Schema(description = "리그 이름", example = "K리그 1")
    private String leagueName;

    @Schema(description = "개인정보 동의 시각", example = ExampleConstants.datetime)
    private LocalDateTime privacyAgreedAt;

    @Schema(description = "마케팅 동의 시각", example = ExampleConstants.datetime)
    private LocalDateTime marketingAgreedAt;

    public GetUserMeDTO(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.providerType = user.getProvider();
        this.profileImageUrl = user.getProfileImageUrl();
        this.teamPk = null;
        this.teamLogoUrl = null;
        this.teamName = null;
        this.leagueLogoUrl = null;
        this.leagueName = null;
        this.privacyAgreedAt = user.getPrivacyAgreedAt();
        this.marketingAgreedAt = user.getMarketingAgreedAt();;
    }
}
