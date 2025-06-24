package kr.kickon.api.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
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
    private Long leaguePk;

    @Schema(description = "리그 한글 이름", example = "K리그")
    private String leagueNameKr;

    @Schema(description = "리그 영어 이름", example = "K-League")
    private String leagueNameEn;


    public TeamDTO(Team team) {
        this.pk = team.getPk();
        this.nameKr = team.getNameKr();
        this.nameEn = team.getNameEn();
        this.logoUrl = team.getLogoUrl();
    }
}
