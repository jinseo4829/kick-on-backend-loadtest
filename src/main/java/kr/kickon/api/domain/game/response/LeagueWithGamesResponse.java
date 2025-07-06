package kr.kickon.api.domain.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.game.dto.GameDTO;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "게임, 리그 response dto")
public class LeagueWithGamesResponse {
    LeagueDTO league;
    List<GameDTO> games;
}
