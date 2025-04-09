package kr.kickon.api.domain.newsReply.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.newsReply.dto.ReplyDTO;
import kr.kickon.api.global.common.ResponseDTO;

import java.util.List;

@Schema(description = "댓글 리스트 조회 응답")
public class GetNewsRepliesResponseDTO extends ResponseDTO<List<ReplyDTO>> {
}
