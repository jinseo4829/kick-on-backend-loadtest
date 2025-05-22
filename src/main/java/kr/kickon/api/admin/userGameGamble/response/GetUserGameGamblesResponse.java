package kr.kickon.api.admin.userGameGamble.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.admin.userGameGamble.dto.UserGameGambleDTO;
import kr.kickon.api.global.common.ResponseDTO;

@Schema(description = "게임 별 승부예측 리스트 조회 response")
public class GetUserGameGamblesResponse extends ResponseDTO<UserGameGambleDTO> {
}
