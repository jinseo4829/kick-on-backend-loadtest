package kr.kickon.api.domain.boardKick;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.boardKick.request.CreateBoardKickRequestDTO;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.boardKick.BoardKickService;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.BoardKick;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board-kick")
@Tag(name = "게시글 킥 관련")
@Slf4j
public class BoardKickController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UUIDGenerator uuidGenerator;
    private final BoardService boardService;
    private final BoardKickService boardKickService;

    @Operation(summary = "게시글 킥 생성 및 삭제", description = "PK값 옵셔널인데 넘기면 그거 기반으로 삭제 할거임!")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createBoardKick(@RequestBody CreateBoardKickRequestDTO body){
        User user = jwtTokenProvider.getUserFromSecurityContext();

        Board board = boardService.findByPk(body.getBoard());

        if(board==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD);

        String id = uuidGenerator.generateUniqueUUID(boardService::findById);
        boardKickService.save(BoardKick.builder()
                .id(id)
                .board(board)
                .user(user).build());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
