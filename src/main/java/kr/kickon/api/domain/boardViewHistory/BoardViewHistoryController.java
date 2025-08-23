package kr.kickon.api.domain.boardViewHistory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.boardViewHistory.request.CreateBoardViewHistoryRequest;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.BoardViewHistory;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board-view-history")
@Tag(name = "게시글 뷰 관련")
@Slf4j
public class BoardViewHistoryController {
    private final JwtTokenProvider jwtTokenProvider;
    private final BoardViewHistoryService boardViewHistoryService;
    private final BoardService boardService;

    @Operation(summary = "게시글 뷰 생성", description = "비회원도 생성 가능")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createBoardView(@RequestBody CreateBoardViewHistoryRequest body){
        User user = jwtTokenProvider.getUserFromSecurityContext();

        Board board = boardService.findByPk(body.getBoard());

        if(board==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD);

        BoardViewHistory.BoardViewHistoryBuilder builder = BoardViewHistory.builder()
                .board(board);

        if(user!=null) builder.user(user);
        boardViewHistoryService.save(builder.build());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
