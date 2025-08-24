package kr.kickon.api.domain.reportBoard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.reportBoard.request.CreateReportBoardRequestDTO;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.ReportBoard;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ReportStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report-board")
@Tag(name = "게시글 신고 관련")
@Slf4j
public class ReportBoardController {
    private final JwtTokenProvider jwtTokenProvider;
    private final BoardService boardService;
    private final ReportBoardService reportBoardService;

    @Operation(summary = "게시글 신고 생성", description = "게시글 pk, 신고 사유 필수!")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createReportBoard(@RequestBody CreateReportBoardRequestDTO body){
        User user = jwtTokenProvider.getUserFromSecurityContext();

        Board board = boardService.findByPk(body.getBoard());

        if(board==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD);

        reportBoardService.save(ReportBoard.builder()
                .reportedBoard(board)
                .reportStatus(ReportStatus.REPORTED)
                .user(user)
                .reason(body.getReason())
                .build());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
