package kr.kickon.api.domain.shorts.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.kickon.api.domain.shorts.dto.ShortsDTO;
import kr.kickon.api.global.common.ResponseDTO;

@Schema(description = "쇼츠 리스트 조회 응답")
public class GetShortsResponse extends ResponseDTO<List<ShortsDTO>> {

}
