package kr.kickon.api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.team.dto.FavoriteTeamDTO;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ProviderType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

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

    private List<FavoriteTeamDTO> favoriteTeams;
    private LeagueDTO league;

    @Schema(description = "인플루언서 여부", example = "false")
    private Boolean isInfluencer;

    @Schema(description = "구단기자 여부", example = "false")
    private Boolean isReporter=false;

    public UserMeDto(User user) {
        super(user);
        email = user.getEmail();
        providerType = user.getProvider();
        privacyAgreedAt = user.getPrivacyAgreedAt();
        marketingAgreedAt = user.getMarketingAgreedAt();
    }
}
