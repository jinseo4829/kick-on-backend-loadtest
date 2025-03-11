package kr.kickon.api.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "팀 정보 DTO")
public class TeamDTO {
    @Schema(description = "팀 pk", example = "1")
    private Long pk;

    @Schema(description = "팀명", example = "리버풀")
    private String name;

    @Schema(description = "팀 로고 url", example = "https://logo.png")
    private String logoUrl;
}
