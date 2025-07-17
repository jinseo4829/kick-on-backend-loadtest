package kr.kickon.api.domain.boardReply.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.boardReply.dto.ReplyDTO;

import kr.kickon.api.global.common.ResponseDTO;

import java.util.List;

@Schema(description = "댓글 리스트 조회 응답")
public class GetBoardRepliesResponse extends ResponseDTO<List<ReplyDTO>> {
}
