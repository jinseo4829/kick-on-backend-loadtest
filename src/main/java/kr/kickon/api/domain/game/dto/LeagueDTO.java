package kr.kickon.api.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.enums.GambleStatus;
import kr.kickon.api.global.common.enums.GameStatus;
import kr.kickon.api.global.common.enums.PredictedResult;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Schema(name = "GameLeagueDTO", description = "리그, 게임 아이템 DTO")
public class LeagueDTO {
    @Schema(description = "리그 이름", example = "K리그 1")
    private String name;

    @Schema(description = "리그 pk", example = "1")
    private Long pk;

    private List<GameDTO> games;

}

