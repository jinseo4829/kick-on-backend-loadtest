package kr.kickon.api.domain.league.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.global.common.ResponseDTO;

@Schema(description = "리그 게임 리스트 조회 응답")
public class GetLeaguesResponse extends ResponseDTO<LeagueDTO> {
}
