package kr.kickon.api.domain.board;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.board.dto.BoardDetailDTO;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.PaginatedBoardListDTO;
import kr.kickon.api.domain.board.request.CreateBoardRequestDTO;
import kr.kickon.api.domain.board.request.GetBoardsRequestDTO;
import kr.kickon.api.domain.board.response.GetBoardDetailResponse;
import kr.kickon.api.domain.board.response.GetBoardsResponse;
import kr.kickon.api.domain.board.response.GetHomeBoardsResponse;
import kr.kickon.api.domain.boardKick.BoardKickService;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.BadRequestException;
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
@RequestMapping("/api/board")
@Tag(name = "게시글")
@Slf4j
public class BoardController {
    private final BoardService boardService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TeamService teamService;
    private final UserFavoriteTeamService userFavoriteTeamService;
    private final UUIDGenerator uuidGenerator;

    @Operation(summary = "홈화면 함께 볼만한 게시글 리스트 조회", description = "응원팀 여부에 상관없이 최신 게시글 기준으로 10개 리스트 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetHomeBoardsResponse.class))),
    })
    @GetMapping("/home")
    public ResponseEntity<ResponseDTO<List<BoardListDTO>>> getHomeBoards() {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        List<BoardListDTO> boards = boardService.findTop10Boards();
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, boards));
    }

    @Operation(summary = "게시글 생성", description = "회원가입한 유저만 게시글 생성 가능")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createBoard(@Valid @RequestBody CreateBoardRequestDTO request){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        String id = uuidGenerator.generateUniqueUUID(boardService::findById);
        Board board = Board.builder()
                .id(id)
                .user(user)
                .contents(request.getContents())
                .title(request.getTitle())
                .hasImage(request.getHasImage())
                .build();

        if(request.getTeam()!=null){
            Team team  = teamService.findByPk(request.getTeam());
            if(team==null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
            board.setTeam(team);
        }
        boardService.save(board);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

    @Operation(summary = "게시글 리스트 조회", description = "페이징 처리 적용하여 게시글 리스트 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetBoardsResponse.class))),
    })
    @GetMapping("")
    public ResponseEntity<ResponseDTO<List<BoardListDTO>>> getBoards(@Valid @ModelAttribute GetBoardsRequestDTO query){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        UserFavoriteTeam userFavoriteTeam = userFavoriteTeamService.findByUserPk(user.getPk());
        if(query.getTeam()!=null && !userFavoriteTeam.getTeam().getPk().equals(query.getTeam())) throw new ForbiddenException(ResponseCode.FORBIDDEN);
        PaginatedBoardListDTO boards = boardService.findBoardsWithPagination(query.getTeam() != null ? userFavoriteTeam.getTeam().getPk() : null, query.getPage(), query.getSize(), query.getOrder());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, boards.getBoardList(), new PagedMetaDTO(boards.getCurrentPage(), boards.getPageSize(), boards.getTotalItems())));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 PK 값으로 게시글 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetBoardDetailResponse.class))),
    })
    @GetMapping("/{boardPk}")
    public ResponseEntity<ResponseDTO<BoardDetailDTO>> getBoardDetail(@PathVariable Long boardPk){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        BoardDetailDTO boardDetailDTO = boardService.findOneBoardListDTOByPk(boardPk,user.getPk());
        if(boardDetailDTO==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, boardDetailDTO));
    }
}
