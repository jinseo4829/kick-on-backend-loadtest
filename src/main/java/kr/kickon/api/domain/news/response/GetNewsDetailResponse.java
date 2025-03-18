package kr.kickon.api.domain.news.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.news.dto.NewsDetailDTO;
import kr.kickon.api.global.common.ResponseDTO;

@Schema(description = "뉴스 상세 조회 응답")
public class GetNewsDetailResponse extends ResponseDTO<NewsDetailDTO> {
}
