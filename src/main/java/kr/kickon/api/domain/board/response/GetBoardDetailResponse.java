package kr.kickon.api.domain.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.board.dto.BoardDetailDTO;
import kr.kickon.api.global.common.ResponseDTO;
@Schema(description = "게시글 상세 조회 응답")
public class GetBoardDetailResponse extends ResponseDTO<BoardDetailDTO> {
}
