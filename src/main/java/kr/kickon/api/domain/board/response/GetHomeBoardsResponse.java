package kr.kickon.api.domain.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.global.common.ResponseDTO;

import java.util.List;

@Schema(description = "함께 볼만한 게시글 리스트 조회 응답")
public class GetHomeBoardsResponse extends ResponseDTO<List<BoardListDTO>> {
}
