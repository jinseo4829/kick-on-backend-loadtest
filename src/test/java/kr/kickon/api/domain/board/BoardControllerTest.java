package kr.kickon.api.domain.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.domain.board.dto.BoardDetailDTO;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.request.CreateBoardRequest;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.entities.Board;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @DisplayName("게시글 validation 실패 - 필수 입력 값 누락")
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
    void getBoards() {
    }

    @Test
    void getBoardDetail() {
    }

    @Test
    void deleteBoard() {
    }

    @Test
    void updateBoard() {
    }
}