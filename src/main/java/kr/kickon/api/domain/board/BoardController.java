package kr.kickon.api.domain.board;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.response.GetHomeBoardsResponse;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
@Tag(name = "게시글")
@Slf4j
public class BoardController {
    private final BoardService boardService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "홈화면 함께 볼만한 게시글 리스트 조회", description = "응원팀 여부에 상관없이 최신 게시글 기준으로 10개 리스트 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetHomeBoardsResponse.class))),
    })
    @GetMapping("/home")
    public ResponseEntity<ResponseDTO<List<BoardListDTO>>> getEventBoards() {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        List<BoardListDTO> boards = boardService.findTop10Boards();
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, boards));
    }
}
