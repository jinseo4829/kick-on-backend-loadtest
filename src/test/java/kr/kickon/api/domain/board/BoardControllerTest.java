package kr.kickon.api.domain.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.domain.board.dto.BoardDetailDTO;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.PaginatedBoardListDTO;
import kr.kickon.api.domain.board.request.CreateBoardRequest;
import kr.kickon.api.domain.board.request.GetBoardsRequest;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.error.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class BoardControllerTest {

    @InjectMocks
    private BoardController boardController;

    @Mock
    private BoardService boardService;

    @Mock
    private TeamService teamService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    private User mockUser;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(boardController)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper()))
                .build();
        objectMapper = new ObjectMapper();
        mockUser = User.builder()
                .id("user1")
                .nickname("테스트 유저")
                .build();
    }

    @Test
    @DisplayName("홈화면 함께 볼만한 게시글 리스트 조회")
    void getHomeBoards_success() throws Exception {

        // given
        given(boardService.getTop10BoardList()).willReturn(BoardList());

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/board/home")
                .accept(MediaType.APPLICATION_JSON));

        // then
        MvcResult mvcResult = resultActions.andExpect(status().isOk())
                     .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                     .andExpect(jsonPath("$.data", hasSize(10)))
                     .andReturn();

        // verify
        verify(boardService, times(1)).getTop10BoardList();
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createBoard_success() throws Exception {
        // given
        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);

        CreateBoardRequest request = new CreateBoardRequest();
        request.setTitle("테스트 제목");
        request.setContents("테스트 내용");
        request.setHasImage(false);

        Board mockBoard = Board.builder()
                .pk(1L)
                .user(mockUser)
                .title(request.getTitle())
                .contents(request.getContents())
                .build();

        given(boardService.createBoardWithMedia(any(Board.class), any(), any(), any()))
                .willReturn(mockBoard);

        BoardDetailDTO mockDTO = BoardDetailDTO.builder()
                .pk(1L)
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        given(boardService.getBoardDetailDTOByPk(mockBoard.getPk(), mockUser))
                .willReturn(mockDTO);

        // then
        mockMvc.perform(post("/api/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));
    }

    @Test
    @DisplayName("게시글 생성 validation 실패 - 필수 입력 값 누락")
    void createBoard_validationFail() throws Exception {
        // given: title 누락
        CreateBoardRequest request = new CreateBoardRequest();
        request.setContents("테스트 내용");
        request.setHasImage(false);

        // when & then
        mockMvc.perform(post("/api/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 팀 NotFoundException")
    void createBoard_teamNotFound() throws Exception {
        // given
        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);
        given(teamService.findByPk(999L)).willReturn(null);

        CreateBoardRequest request = new CreateBoardRequest();
        request.setTitle("테스트 제목");
        request.setContents("테스트 내용");
        request.setHasImage(false);
        request.setTeam(999L); // 존재하지 않는 팀

        // when & then
        mockMvc.perform(post("/api/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_TEAM"));
    }

    private List<BoardListDTO> BoardList() {
        List<BoardListDTO> boardList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            BaseUserDTO mockUser = BaseUserDTO.builder()
                    .id("user1")
                    .nickname("테스트 유저")
                    .profileImageUrl("https://example.com/profile.png")
                    .isReporter(false)
                    .build();

            TeamDTO mockTeam = TeamDTO.builder()
                    .pk(1L)
                    .nameKr("FC 킥온")
                    .nameEn("FC Kickon")
                    .logoUrl("https://example.com/logo.png")
                    .leaguePk(10L)
                    .leagueNameKr("K리그")
                    .leagueNameEn("K-League")
                    .leagueLogoUrl("https://example.com/league-logo.png")
                    .build();

            BoardListDTO mockBoard =
                    BoardListDTO.builder()
                            .pk(1L)
                            .title("테스트 글")
                            .user(mockUser)
                            .team(mockTeam)
                            .createdAt(LocalDateTime.of(2025, 9, 1, 12, 0))
                            .hasImage(true)
                            .views(100)
                            .likes(10)
                            .replies(2)
                            .isPinned(false)
                            .build();

            boardList.add(mockBoard);
        }
        return boardList;
    }


    @Test
    @DisplayName("게시글 리스트 조회 성공")
    void getBoards_success() throws Exception {
        // given
        GetBoardsRequest request = new GetBoardsRequest();
        request.setPage(1);
        request.setSize(10);
        request.setOrder("recent");
        request.setInfinite(false);

        PaginatedBoardListDTO mockResult = new PaginatedBoardListDTO();
        mockResult.setBoardList(createMockBoardList(10));
        mockResult.setCurrentPage(1);
        mockResult.setPageSize(10);
        mockResult.setTotalItems(100L);
        mockResult.setHasNext(null);

        given(boardService.getBoardListWithPagination(
                eq(null), anyInt(), anyInt(), anyString(), eq(false), any(), any()))
                .willReturn(mockResult);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/board")
                .param("page", String.valueOf(request.getPage()))
                .param("size", String.valueOf(request.getSize()))
                .param("order", request.getOrder())
                .param("infinite", String.valueOf(request.getInfinite()))
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data", hasSize(10)))
                .andExpect(jsonPath("$.meta.currentPage").value(1))
                .andExpect(jsonPath("$.meta.pageSize").value(10))
                .andExpect(jsonPath("$.meta.totalItems").value(100));

        // verify
        verify(boardService, times(1)).getBoardListWithPagination(
                eq(null), anyInt(), anyInt(), anyString(), eq(false), any(), any());
    }

    @Test
    @DisplayName("게시글 리스트 조회 실패 - 팀 NotFoundException")
    void getBoards_teamNotFound() throws Exception {
        // given
        GetBoardsRequest request = new GetBoardsRequest();
        request.setTeam(999L); // 존재하지 않는 팀 PK
        request.setPage(1);
        request.setSize(10);
        request.setOrder("recent");

        given(teamService.findByPk(anyLong())).willReturn(null);

        // when & then
        mockMvc.perform(get("/api/board")
                        .param("team", String.valueOf(request.getTeam()))
                        .param("page", String.valueOf(request.getPage()))
                        .param("size", String.valueOf(request.getSize()))
                        .param("order", request.getOrder())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_TEAM"));

        // verify
        verify(teamService, times(1)).findByPk(anyLong());
        verify(boardService, never()).getBoardListWithPagination(
                any(), anyInt(), anyInt(), anyString(), any(), any(), any());
    }

    private List<BoardListDTO> createMockBoardList(int size) {
        List<BoardListDTO> boardList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            BoardListDTO mockBoard = BoardListDTO.builder()
                    .pk((long) i)
                    .title("테스트 제목 " + i)
                    .build();
            boardList.add(mockBoard);
        }
        return boardList;
    }

    @Test
    @DisplayName("게시글 리스트 조회 validation 실패 - 필수 입력 값 누락")
    void getBoards_validationFail() throws Exception {
        // given: order 누락
        GetBoardsRequest request = new GetBoardsRequest();
        request.setPage(1);
        request.setSize(10);

        // when & then
        mockMvc.perform(get("/api/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getBoardDetail_success() throws Exception {
        // given
        Long boardPk = 1L;
        User mockUser = User.builder().id("user1").build();
        BoardDetailDTO mockBoardDetail = BoardDetailDTO.builder()
                .pk(boardPk)
                .title("테스트 게시글")
                .content("테스트 내용")
                .build();

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);
        given(boardService.getBoardDetailDTOByPk(eq(boardPk), eq(mockUser))).willReturn(mockBoardDetail);

        // when & then
        mockMvc.perform(get("/api/board/{boardPk}", boardPk)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data.pk").value(boardPk))
                .andExpect(jsonPath("$.data.title").value("테스트 게시글"));

        // verify
        verify(boardService, times(1)).getBoardDetailDTOByPk(eq(boardPk), eq(mockUser));
    }

    @Test
    @DisplayName("게시글 상세 조회 실패 - 게시글 NotFoundException")
    void getBoardDetail_boardNotFound() throws Exception {
        // given
        Long boardPk = 999L; // 존재하지 않는 게시글 PK
        User mockUser = User.builder().id("user1").build();

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);
        given(boardService.getBoardDetailDTOByPk(eq(boardPk), eq(mockUser))).willReturn(null);

        // when & then
        mockMvc.perform(get("/api/board/{boardPk}", boardPk)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_BOARD"));

        // verify
        verify(boardService, times(1)).getBoardDetailDTOByPk(eq(boardPk), eq(mockUser));
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deleteBoard_success() throws Exception {
        // given
        Long boardPk = 1L;
        String userId = "user1";
        User mockUser = User.builder().id(userId).build();

        Board mockBoard = Board.builder()
                .pk(boardPk)
                .user(mockUser)
                .build();

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);
        given(boardService.findByPk(eq(boardPk))).willReturn(mockBoard);
        doNothing().when(boardService).deleteBoard(any(Board.class));

        // when & then
        mockMvc.perform(delete("/api/board/{boardPk}", boardPk)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));

        // verify
        verify(boardService, times(1)).findByPk(eq(boardPk));
        verify(boardService, times(1)).deleteBoard(any(Board.class));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 게시글 NotFoundException")
    void deleteBoard_boardNotFound() throws Exception {
        // given
        Long boardPk = 999L; // 존재하지 않는 게시글 PK
        User mockUser = User.builder().id("user1").build();

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);
        given(boardService.findByPk(eq(boardPk))).willReturn(null);

        // when & then
        mockMvc.perform(delete("/api/board/{boardPk}", boardPk)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_BOARD"));

        // verify
        verify(boardService, times(1)).findByPk(eq(boardPk));
        verify(boardService, never()).deleteBoard(any(Board.class));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void deleteBoard_forbidden() throws Exception {
        // given
        Long boardPk = 1L;
        String requesterId = "user1";
        String boardOwnerId = "anotherUser";

        User mockRequester = User.builder().id(requesterId).build();
        User mockBoardOwner = User.builder().id(boardOwnerId).build();

        Board mockBoard = Board.builder()
                .pk(boardPk)
                .user(mockBoardOwner)
                .build();

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockRequester);
        given(boardService.findByPk(eq(boardPk))).willReturn(mockBoard);

        // when & then
        mockMvc.perform(delete("/api/board/{boardPk}", boardPk)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        // verify
        verify(boardService, times(1)).findByPk(eq(boardPk));
        verify(boardService, never()).deleteBoard(any(Board.class));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updateBoard_success() throws Exception {
        // given
        Long boardPk = 1L;
        String userId = "user1";
        User mockUser = User.builder().id(userId).build();

        CreateBoardRequest request = new CreateBoardRequest();
        request.setTitle("수정된 제목");
        request.setContents("수정된 내용");
        request.setHasImage(true);
        request.setIsPinned(true);
        request.setTeam(1L);

        Board mockBoard = Board.builder()
                .pk(boardPk)
                .user(mockUser)
                .build();

        BoardDetailDTO updatedBoardDetail = BoardDetailDTO.builder()
                .pk(boardPk)
                .title("수정된 제목")
                .content("수정된 내용")
                .hasImage(true)
                .isPinned(true)
                .build();

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);
        given(boardService.findByPk(eq(boardPk))).willReturn(mockBoard);
        given(teamService.findByPk(anyLong())).willReturn(Team.builder().pk(1L).build());
        given(boardService.getBoardDetailDTOByPk(eq(boardPk), eq(mockUser))).willReturn(updatedBoardDetail);

        // when & then
        mockMvc.perform(patch("/api/board/{boardPk}", boardPk)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"));

        // verify
        verify(boardService, times(1)).findByPk(eq(boardPk));
        verify(boardService, times(1)).updateBoard(
                any(Board.class), any(), any(), any());
        verify(boardService, times(1)).getBoardDetailDTOByPk(eq(boardPk), eq(mockUser));
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글 NotFoundException")
    void updateBoard_boardNotFound() throws Exception {
        // given
        Long boardPk = 999L; // 존재하지 않는 게시글 PK
        User mockUser = User.builder().id("user1").build();

        CreateBoardRequest request = new CreateBoardRequest();
        request.setTitle("수정된 제목");
        request.setContents("수정된 내용");
        request.setHasImage(true);

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);
        given(boardService.findByPk(eq(boardPk))).willReturn(null);

        // when & then
        mockMvc.perform(patch("/api/board/{boardPk}", boardPk)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_BOARD"));

        // verify
        verify(boardService, times(1)).findByPk(eq(boardPk));
        verify(boardService, never()).updateBoard(any(), any(), any(), any());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 권한 없음")
    void updateBoard_forbidden() throws Exception {
        // given
        Long boardPk = 1L;
        String requesterId = "user1";
        String boardOwnerId = "anotherUser";

        User mockRequester = User.builder().id(requesterId).build();
        User mockBoardOwner = User.builder().id(boardOwnerId).build();

        CreateBoardRequest request = new CreateBoardRequest();
        request.setTitle("수정된 제목");
        request.setContents("수정된 내용");
        request.setHasImage(true);

        Board mockBoard = Board.builder()
                .pk(boardPk)
                .user(mockBoardOwner)
                .build();

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockRequester);
        given(boardService.findByPk(eq(boardPk))).willReturn(mockBoard);

        // when & then
        mockMvc.perform(patch("/api/board/{boardPk}", boardPk)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        // verify
        verify(boardService, times(1)).findByPk(eq(boardPk));
        verify(boardService, never()).updateBoard(any(), any(), any(), any());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 팀 NotFoundException")
    void updateBoard_teamNotFound() throws Exception {
        // given
        Long boardPk = 1L;
        String userId = "user1";
        User mockUser = User.builder().id(userId).build();

        CreateBoardRequest request = new CreateBoardRequest();
        request.setTitle("수정된 제목");
        request.setContents("수정된 내용");
        request.setHasImage(true);
        request.setTeam(999L); // 존재하지 않는 팀 PK

        Board mockBoard = Board.builder()
                .pk(boardPk)
                .user(mockUser)
                .build();

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);
        given(boardService.findByPk(eq(boardPk))).willReturn(mockBoard);
        given(teamService.findByPk(anyLong())).willReturn(null); // teamService가 null 반환

        // when & then
        mockMvc.perform(patch("/api/board/{boardPk}", boardPk)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_TEAM"));

        // verify
        verify(boardService, times(1)).findByPk(eq(boardPk));
        verify(teamService, times(1)).findByPk(anyLong());
        verify(boardService, never()).updateBoard(any(), any(), any(), any());
    }

    @Test
    @DisplayName("게시글 수정 validation 실패 - 필수 입력 값 누락")
    void updateBoard_validationFail() throws Exception {
        // given: hasImage 누락
        Long boardPk = 1L;
        CreateBoardRequest request = new CreateBoardRequest();
        request.setTitle("수정된 제목");
        request.setContents("수정된 내용");

        // when & then
        mockMvc.perform(patch("/api/board/{boardPk}", boardPk)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}