package kr.kickon.api.domain.boardViewHistory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.boardViewHistory.request.CreateBoardViewHistory;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.BoardViewHistory;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
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
    private final UUIDGenerator uuidGenerator;
    private final BoardService boardService;

    @Operation(summary = "게시글 뷰 생성", description = "비회원도 생성 가능")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createBoardView(@RequestBody CreateBoardViewHistory body){
        User user = jwtTokenProvider.getUserFromSecurityContext();

        Board board = boardService.findByPk(body.getBoard());

        if(board==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD);

        String id = uuidGenerator.generateUniqueUUID(boardViewHistoryService::findById);

        BoardViewHistory.BoardViewHistoryBuilder builder = BoardViewHistory.builder()
                .id(id)
                .board(board);

        if(user!=null) builder.user(user);
        boardViewHistoryService.save(builder.build());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
