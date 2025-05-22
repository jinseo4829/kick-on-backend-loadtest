package kr.kickon.api.admin.userGameGamble.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.enums.GambleStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "AdminUserGameGambleDTO", description = "게임 예측 응답 DTO")
public class UserGameGambleDTO {

    @Schema(description = "유저 게임 예측 PK", example = "101")
    private Long pk;

    @Schema(description = "예측 유저 정보")
    private UserSummary user;

    @Schema(description = "서포팅 팀 정보")
    private TeamSummary supportingTeam;

    @Schema(description = "예측 홈팀 점수", example = "2")
    private Integer predictedHomeScore;

    @Schema(description = "예측 원정팀 점수", example = "1")
    private Integer predictedAwayScore;

    @Schema(description = "예측 결과 상태", example = "COMPLETED")
    private GambleStatus gambleStatus;

    @Data
    @Builder
    @Schema(description = "유저 요약 정보")
    public static class UserSummary {
        @Schema(description = "유저 PK", example = "3")
        private Long pk;

        @Schema(description = "유저 닉네임", example = "축알못123")
        private String nickname;
    }

    @Data
    @Builder
    @Schema(description = "팀 요약 정보")
    public static class TeamSummary {
        @Schema(description = "팀 PK", example = "18")
        private Long pk;

        @Schema(description = "팀 한글 이름", example = "울산 현대")
        private String nameKr;

        @Schema(description = "팀 영어 이름", example = "Ulsan Hyundai")
        private String nameEn;
    }
}