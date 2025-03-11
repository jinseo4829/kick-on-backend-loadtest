package kr.kickon.api.domain.eventBoard.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.eventBoard.dto.GetEventBoardDTO;
import kr.kickon.api.global.common.ResponseDTO;

import java.util.List;

@Schema(name = "GetEventBoardsResponse", description = "이벤트 게시글 리스트 조회 응답")
public class GetEventBoardsResponse extends ResponseDTO<List<GetEventBoardDTO>> {

}
