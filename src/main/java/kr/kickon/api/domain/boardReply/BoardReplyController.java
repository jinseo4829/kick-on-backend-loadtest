package kr.kickon.api.domain.boardReply;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.boardReply.dto.PaginatedReplyListDTO;
import kr.kickon.api.domain.boardReply.dto.ReplyDTO;
import kr.kickon.api.domain.boardReply.request.CreateBoardReplyRequestDTO;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.boardReply.request.GetBoardRepliesRequestDTO;
import kr.kickon.api.domain.boardReply.request.PatchBoardReplyRequestDTO;
import kr.kickon.api.domain.boardReply.response.GetBoardRepliesResponseDTO;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.ForbiddenException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board-reply")
@Tag(name = "게시글 댓글")
@Slf4j
public class BoardReplyController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserFavoriteTeamService userFavoriteTeamService;
    private final BoardReplyService boardReplyService;
    private final UUIDGenerator uuidGenerator;
    private final BoardService boardService;

    @Operation(summary = "게시글 댓글 생성", description = "회원가입한 유저만 게시글 댓글 생성 가능")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createBoardReply(@Valid @RequestBody CreateBoardReplyRequestDTO request){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        Board board = boardService.findByPk(request.getBoard());
        if(board == null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD);

        if(board.getTeam()!= null){
            UserFavoriteTeam userFavoriteTeam = userFavoriteTeamService.findByUserPk(user.getPk());
            if(userFavoriteTeam==null) throw new ForbiddenException(ResponseCode.FORBIDDEN);
            if(!userFavoriteTeam.getTeam().getPk().equals(board.getTeam().getPk())) throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }

        String id = uuidGenerator.generateUniqueUUID(boardReplyService::findById);
        BoardReply.BoardReplyBuilder boardReplyBuilder = BoardReply.builder()
                .id(id)
                .user(user)
                .board(board)
                .contents(request.getContents());

        if(request.getParentReply()!=null) {
            BoardReply parentBoardReply = boardReplyService.findByPk(request.getParentReply());
            if(parentBoardReply == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS);
            boardReplyBuilder.parentBoardReply(parentBoardReply);
        }

        BoardReply boardReply = boardReplyBuilder.build();
        boardReply = boardReplyService.createBoardReplyWithImages(boardReply,request.getUsedImageKeys());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

    @Operation(summary = "게시글 댓글 리스트 조회", description = "게시글 댓글 페이징 처리해서 전달")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetBoardRepliesResponseDTO.class))),
    })
    @GetMapping()
    public ResponseEntity<ResponseDTO<List<ReplyDTO>>> getBoardReplies(@Valid GetBoardRepliesRequestDTO query){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        Board boardData = boardService.findByPk(query.getBoard());
        if(boardData == null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD);
        PaginatedReplyListDTO paginatedReplyListDTO = boardReplyService.getRepliesByBoard(query.getBoard(),user!=null ? user.getPk() : null, query.getPage(), query.getSize(), query.getInfinite() != null ? query.getInfinite() : null, query.getLastReply());
        if(paginatedReplyListDTO.getHasNext()!=null){
            return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,paginatedReplyListDTO.getReplyList(),
                    new PagedMetaDTO(paginatedReplyListDTO.getHasNext())));
        }else{
            return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,paginatedReplyListDTO.getReplyList(),
                    new PagedMetaDTO(paginatedReplyListDTO.getCurrentPage(), paginatedReplyListDTO.getPageSize(), paginatedReplyListDTO.getTotalItems())));
        }
    }

    @Operation(summary = "게시글 댓글 삭제", description = "게시글 댓글 PK값으로 댓글 삭제")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
    })
    @DeleteMapping("/{boardReplyPk}")
    public ResponseEntity<ResponseDTO> deleteBoardReplies(@PathVariable Long boardReplyPk){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        BoardReply boardReplyData = boardReplyService.findByPk(boardReplyPk);
        if(boardReplyData == null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD_REPLY);
        if (!boardReplyData.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }        boardReplyService.deleteBoardReply(boardReplyData);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

    @Operation(summary = "게시글 댓글 수정", description = "게시글 댓글 PK값으로 댓글 수정")
    @PatchMapping("/{boardReplyPk}")
    public ResponseEntity<ResponseDTO<Void>> patchBoardReply(@PathVariable Long boardReplyPk,
        @Valid @RequestBody PatchBoardReplyRequestDTO request){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        BoardReply boardReplyData = boardReplyService.findByPk(boardReplyPk);
        if(boardReplyData == null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD_REPLY);
        if (!boardReplyData.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }
        boardReplyData.setContents(request.getContents());

        boardReplyService.patchBoardReplyWithImages(boardReplyData,request.getUsedImageKeys());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
