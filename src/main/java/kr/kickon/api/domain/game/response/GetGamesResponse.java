package kr.kickon.api.domain.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ResponseDTO;
@Schema(description = "홈 게임 리스트 조회 응답")
public class GetGamesResponse extends ResponseDTO<LeagueWithGamesDTO> {
}
