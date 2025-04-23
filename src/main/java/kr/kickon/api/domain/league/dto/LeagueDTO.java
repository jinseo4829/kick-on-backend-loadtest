package kr.kickon.api.domain.league.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.enums.LeagueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@Schema(description = "리그 리스트 조회를 위한 DTO")
@AllArgsConstructor
public class LeagueDTO {
    @Schema(example = "1", description = "리그 pk값")
    private Long pk;

    @Schema(example = ExampleConstants.leaguenameEn, description = "리그 영어 제목")
    private String nameEn;

    @Schema(example = ExampleConstants.leagueKrName, description = "리그 한글 제목")
    private String nameKr;

    @Schema(example = ExampleConstants.teamLogoUrl, description = "리그 로고 url")
    private String logoUrl;

    @Schema(example = LeagueType.VALUE.League, description = "리그 타입")
    private LeagueType type;

    public LeagueDTO(League league) {
        this.pk = league.getPk();
        this.nameEn = league.getNameEn();
        this.nameKr = league.getNameKr();
        this.logoUrl = league.getLogoUrl();
        this.type = league.getType();
    }
}