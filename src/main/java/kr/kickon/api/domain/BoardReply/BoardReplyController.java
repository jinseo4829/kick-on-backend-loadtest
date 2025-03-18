package kr.kickon.api.domain.BoardReply;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.BoardReply.request.CreateBoardReplyRequestDTO;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.BoardReply;
import kr.kickon.api.global.common.entities.NewsReply;
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
@RequestMapping("/api/board-reply")
@Tag(name = "게시글 댓글")
@Slf4j
public class BoardReplyController {
    private final JwtTokenProvider jwtTokenProvider;
    private final BoardReplyService boardReplyService;
    private final UUIDGenerator uuidGenerator;
    private final BoardService boardService;

    @Operation(summary = "게시글 댓글 생성", description = "회원가입한 유저만 게시글 댓글 생성 가능")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createBoardReply(@Valid @RequestBody CreateBoardReplyRequestDTO request){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        Board board = boardService.findByPk(request.getBoard());
        if(board == null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD);
        String id = uuidGenerator.generateUniqueUUID(boardReplyService::findById);
        BoardReply.BoardReplyBuilder boardReplyBuilder = BoardReply.builder()
                .id(id)
                .user(user)
                .board(board)
                .contents(request.getContents());

        if(request.getParentBoard()!=null) {
            BoardReply parentBoardReply = boardReplyService.findByPk(request.getParentBoard());
            if(parentBoardReply == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS);
            boardReplyBuilder.parentBoardReply(parentBoardReply);
        }

        BoardReply boardReply = boardReplyBuilder.build();

        boardReplyService.save(boardReply);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
