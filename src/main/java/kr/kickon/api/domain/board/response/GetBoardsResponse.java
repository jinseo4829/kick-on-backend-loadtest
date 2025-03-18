package kr.kickon.api.domain.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.PaginatedBoardListDTO;
import kr.kickon.api.global.common.ResponseDTO;

import java.util.List;

@Schema(description = "게시글 리스트 조회 응답")
public class GetBoardsResponse extends ResponseDTO<List<BoardListDTO>> {
}
