package kr.kickon.api.admin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.enums.ProviderType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Schema(name = "AdminUserDetailDTO", description = "유저 상세 조회 응답 DTO")
public class UserDetailDTO {

    @Schema(description = "유저 PK", example = "1")
    private Long pk;

    @Schema(description = "유저 닉네임", example = "kickon_user")
    private String nickname;

    @Schema(description = "유저 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "소셜 로그인 제공자", example = "KAKAO / NAVER")
    private ProviderType provider;

    @Schema(description = "개인정보 처리방침 동의 시각", example = "2024-01-01T12:00:00")
    private LocalDateTime privacyAgreedAt;

    @Schema(description = "마케팅 수신 동의 시각", example = "2024-01-02T15:00:00")
    private LocalDateTime marketingAgreedAt;

    @Schema(description = "회원 가입 시각", example = "2024-01-01T11:11:11")
    private LocalDateTime createdAt;

    @Schema(description = "유저 신고/제재 사유", example = "부적절한 닉네임 사용")
    private String reason;

    @Schema(description = "유저가 응원하는 팀 목록")
    private List<TeamDTO> favoriteTeams;

    public UserDetailDTO(User user, List<UserFavoriteTeam> favoriteTeams) {
        this.pk = user.getPk();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.provider = user.getProvider();
        this.privacyAgreedAt = user.getPrivacyAgreedAt();
        this.marketingAgreedAt = user.getMarketingAgreedAt();
        this.createdAt = user.getCreatedAt();
        this.reason = user.getReason();
        this.favoriteTeams = favoriteTeams.stream()
                .map(ft -> new TeamDTO(ft.getTeam()))
                .collect(Collectors.toList());
    }
}