package kr.kickon.api.domain.userPointEvent.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.userPointEvent.dto.UserRankingDTO;
import kr.kickon.api.global.common.ResponseDTO;

@Schema(description = "유저 포인트 랭킹 리스트 조회 응답")
public class GetUserPointRankingResponse extends ResponseDTO<UserRankingDTO> {

}
