package kr.kickon.api.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TeamDTO {
    @Schema(description = "사용자 고유 pk", example = "1")
    private Long pk;

    @Schema(description = "한글 이름", example = ExampleConstants.teamKrName)
    private String nameKr;

    @Schema(description = "영어 이름", example = ExampleConstants.teamEnName)
    private String nameEn;

    @Schema(description = "팀 url", example = ExampleConstants.teamLogoUrl)
    private String logoUrl;
}
