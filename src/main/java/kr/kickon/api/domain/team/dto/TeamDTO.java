package kr.kickon.api.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.Team;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TeamDTO {

    @Schema(description = "사용자 고유 pk", example = "1")
    private Long pk;

    @Schema(description = "한글 이름", example = ExampleConstants.teamKrName)
    private String nameKr;

    @Schema(description = "영어 이름", example = ExampleConstants.teamnameEn)
    private String nameEn;

    @Schema(description = "팀 url", example = ExampleConstants.teamLogoUrl)
    private String logoUrl;

    @Schema(description = "리그 고유 PK", example = "10")
    @Builder.Default
    private Long leaguePk = null;

    @Schema(description = "리그 한글 이름", example = "K리그")
    @Builder.Default
    private String leagueNameKr = "";

    @Schema(description = "리그 영어 이름", example = "K-League")
    @Builder.Default
    private String leagueNameEn = "";

    @Schema(description = "응원팀이 속한 리그 로고 URL", example = "https://example.com/league-logo.png")
    private String leagueLogoUrl;

    public TeamDTO(Team team) {
        this.pk = team.getPk();
        this.nameKr = team.getNameKr();
        this.nameEn = team.getNameEn();
        this.logoUrl = team.getLogoUrl();
    }

    public static TeamDTO fromTeamAndLeague(Team team, League league) {
        TeamDTO.TeamDTOBuilder<?,?> builder = TeamDTO.builder()
            .pk(team.getPk())
            .nameKr(team.getNameKr())
            .nameEn(team.getNameEn())
            .logoUrl(team.getLogoUrl());

        if (league != null) {
            builder.leaguePk(league.getPk())
                .leagueNameKr(league.getNameKr())
                .leagueNameEn(league.getNameEn())
                    .leagueLogoUrl(league.getLogoUrl());
        }
        return builder.build();
    }
}
