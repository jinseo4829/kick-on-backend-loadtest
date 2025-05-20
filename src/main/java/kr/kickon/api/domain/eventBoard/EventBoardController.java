package kr.kickon.api.domain.eventBoard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.eventBoard.dto.GetEventBoardDTO;
import kr.kickon.api.domain.eventBoard.response.GetEventBoardsResponse;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
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
@Tag(name = "배너 게시글")
@Slf4j
public class EventBoardController {
    private final EventBoardService eventBoardService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "배너 게시글 리스트 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetEventBoardsResponse.class))),
    })
    @GetMapping()
    public ResponseEntity<ResponseDTO<List<GetEventBoardDTO>>> getEventBoards() {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        List<GetEventBoardDTO> eventBoards = eventBoardService.findAll();
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, eventBoards));
    }

}
