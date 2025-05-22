package kr.kickon.api.admin.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.admin.game.dto.GameDetailDTO;
import kr.kickon.api.admin.game.dto.GameListDTO;
import kr.kickon.api.global.common.ResponseDTO;

import java.util.List;

@Schema(name = "AdminGetGameDetailResponse", description = "게임 상세 조회 (어드민)")
public class GetGameDetailResponse extends ResponseDTO<GameDetailDTO> {
}
