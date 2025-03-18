package kr.kickon.api.domain.news.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.news.dto.HotNewsListDTO;
import kr.kickon.api.domain.news.dto.NewsListDTO;
import kr.kickon.api.global.common.ResponseDTO;

import java.util.List;

@Schema(description = "조회수 높은 뉴스 리스트 조회 응답")
public class GetHotNewsResponse extends ResponseDTO<List<HotNewsListDTO>> {
}
