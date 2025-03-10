package kr.kickon.api.domain.gambleSeasonRanking.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.global.common.ResponseDTO;

import java.util.List;

@Schema(name = "GetGambleSeasonRankingResponse", description = "승부예측 리그 순위 리스트 조회 응답")
public class GetGambleSeasonRankingResponse extends ResponseDTO<List<GetGambleSeasonRankingDTO>> {
}
