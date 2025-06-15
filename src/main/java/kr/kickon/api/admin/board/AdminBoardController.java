package kr.kickon.api.admin.board;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.admin.board.response.AdminGetBoardDetailResponse;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/board")
@Tag(name = "게시글")
@Slf4j
public class AdminBoardController {
    private final BoardService boardService;

    @Operation(summary = "게시글 상세 조회", description = "게시글 PK 값으로 게시글 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = AdminGetBoardDetailResponse.class))),
    })
    @GetMapping("/{boardPk}")
    public ResponseEntity<ResponseDTO<Board>> getBoardDetail(@PathVariable Long boardPk){
        Board board = boardService.findByPk(boardPk);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, board));
    }
}
