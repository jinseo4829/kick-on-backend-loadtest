package kr.kickon.api.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class FavoriteTeamDTO extends TeamDTO{
    @Schema(description = "우선순위", example = "3")
    private Integer priorityNum;

    @Schema(description = "리그 고유 PK", example = "10")
    private Long leaguePk;

    @Schema(description = "리그 한글 이름", example = "K리그")
    private String leagueNameKr;

    @Schema(description = "리그 영어 이름", example = "K-League")
    private String leagueNameEn;

    @Schema(description = "응원팀이 속한 리그 로고 URL", example = "https://example.com/league-logo.png")
    private String leagueLogoUrl;
}
