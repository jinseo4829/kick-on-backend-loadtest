package kr.kickon.api.admin.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.enums.GameStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "게임 리스트 응답 DTO")
public class GameListDTO {

    @Schema(description = "게임 PK", example = "1564")
    private Long pk;

    @Schema(description = "홈 팀 정보")
    private TeamSummary homeTeam;

    @Schema(description = "어웨이 팀 정보")
    private TeamSummary awayTeam;

    @Schema(description = "게임 상태", example = "PENDING")
    private GameStatus gameStatus;

    @Schema(description = "실제 시즌 정보")
    private ActualSeasonSummary actualSeason;

    @Schema(description = "기타 정보", nullable = true)
    private String etc;

    @Data
    @Builder
    @Schema(description = "팀 요약 정보")
    public static class TeamSummary {

        @Schema(description = "팀 PK", example = "1658")
        private Long pk;

        @Schema(description = "팀 이름 (한글)", example = "서울")
        private String nameKr;

        @Schema(description = "팀 이름 (영문)", example = "FC Seoul")
        private String nameEn;

        @Schema(description = "팀 로고 URL", example = "https://media.api-sports.io/football/teams/2766.png")
        private String logoUrl;
    }

    @Data
    @Builder
    @Schema(description = "시즌 요약 정보")
    public static class ActualSeasonSummary {

        @Schema(description = "시즌 PK", example = "21")
        private Long pk;

        @Schema(description = "시즌 제목", example = "K리그")
        private String title;

        @Schema(description = "시즌 연도", example = "2025")
        private Integer year;

        @Schema(description = "시즌 운영 상태", example = "PROCEEDING")
        private OperatingStatus operatingStatus;
    }
}