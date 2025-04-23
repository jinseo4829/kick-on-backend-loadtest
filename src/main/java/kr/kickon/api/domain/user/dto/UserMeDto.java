package kr.kickon.api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ProviderType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "내 정보 DTO")
@SuperBuilder
@NoArgsConstructor
public class UserMeDto extends BaseUserDTO {
    @Schema(description = "이메일", example = "limms1217@naver.com")
    private String email;

    @Schema(description = "소셜 로그인 provider", example = "KAKAO")
    private ProviderType providerType;

    @Schema(description = "개인정보 동의 시각", example = ExampleConstants.datetime)
    private LocalDateTime privacyAgreedAt;

    @Schema(description = "마케팅 동의 시각", example = ExampleConstants.datetime)
    private LocalDateTime marketingAgreedAt;

    private TeamDTO favoriteTeam;

    private LeagueDTO league;

    public UserMeDto(User user) {
        super(user);
        email = user.getEmail();
        providerType = user.getProvider();
        privacyAgreedAt = user.getPrivacyAgreedAt();
        marketingAgreedAt = user.getMarketingAgreedAt();
    }
}
