package kr.kickon.api.domain.eventBoard;

import io.swagger.v3.oas.annotations.Operation;
import kr.kickon.api.domain.eventBoard.dto.GetEventBoardDTO;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event-board")
@Slf4j
public class EventBoardController {
    private final EventBoardService eventBoardService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "배너 게시글 리스트 조회")
    @GetMapping()
    public ResponseEntity<ResponseDTO<List<GetEventBoardDTO>>> getEventBoards() {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        List<GetEventBoardDTO> eventBoards = eventBoardService.findAll();
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, eventBoards));
    }

}
