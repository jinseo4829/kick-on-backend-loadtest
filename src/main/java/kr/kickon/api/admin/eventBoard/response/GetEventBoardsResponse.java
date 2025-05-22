package kr.kickon.api.admin.eventBoard.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.EventBoard;

import java.util.List;

@Schema(name = "AdminGetEventBoardsResponse", description = "이벤트 배너 리스트 응답")
public class GetEventBoardsResponse extends ResponseDTO<List<EventBoard>> {
}
