package kr.kickon.api.domain.boardReplyKick;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.boardReply.BoardReplyService;
import kr.kickon.api.domain.boardReplyKick.request.CreateBoardReplyKickRequestDTO;
import kr.kickon.api.domain.boardViewHistory.BoardViewHistoryService;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
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
@RequestMapping("/api/board-reply-kick")
@Tag(name = "게시글 댓글 킥 관련")
@Slf4j
public class BoardReplyKickController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UUIDGenerator uuidGenerator;
    private final BoardReplyService boardReplyService;
    private final BoardReplyKickService boardReplyKickService;

    @Operation(summary = "게시글 댓글 킥 생성 및 삭제", description = "댓글 PK값 옵셔널인데 넘기면 그거 기반으로 삭제 할거임!")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createBoardReplyKick(@RequestBody CreateBoardReplyKickRequestDTO body){
        User user = jwtTokenProvider.getUserFromSecurityContext();

        BoardReply boardReply = boardReplyService.findByPk(body.getReply());

        if(boardReply==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD_REPLY);

        // 게시글 댓글 킥 이미 있는지 체크
        BoardReplyKick boardReplyKick = boardReplyKickService.findByBoardReplyAndUser(boardReply.getPk(), user.getPk());
        if(boardReplyKick==null){
            String id = uuidGenerator.generateUniqueUUID(boardReplyService::findById);
            boardReplyKickService.save(BoardReplyKick.builder()
                    .id(id)
                    .boardReply(boardReply)
                    .user(user).build());
        }else{
            boardReplyKick.setStatus(DataStatus.DEACTIVATED);
            boardReplyKickService.save(boardReplyKick);
        }
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
