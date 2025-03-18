package kr.kickon.api.domain.actualSeasonRanking.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.actualSeasonRanking.dto.GetActualSeasonRankingDTO;
import kr.kickon.api.global.common.ResponseDTO;

import java.util.List;

@Schema(name = "GetActualSeasonRankingResponse", description = "실제 리그 순위 리스트 조회 응답")
public class GetActualSeasonRankingResponse extends ResponseDTO<List<GetActualSeasonRankingDTO>> {
}
