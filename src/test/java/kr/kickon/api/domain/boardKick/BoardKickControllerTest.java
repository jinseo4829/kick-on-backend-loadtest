package kr.kickon.api.domain.boardKick;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.boardKick.request.CreateBoardKickRequest;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.BoardKick;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.error.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BoardKickControllerTest {

    @InjectMocks
    private BoardKickController boardKickController;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private BoardService boardService;

    @Mock
    private BoardKickService boardKickService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(boardKickController)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper()))
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("게시글 킥 생성 성공 - 기존 Kick 없는 경우 새로 저장")
    void createBoardKick_newKick_success() throws Exception {
        // given
        User mockUser = User.builder().pk(1L).build();
        Board mockBoard = Board.builder().pk(10L).build();

        CreateBoardKickRequest  request = new CreateBoardKickRequest();
        request.setBoard(10L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
        when(boardService.findByPk(anyLong())).thenReturn(mockBoard);
        when(boardKickService.findByBoardAndUser(anyLong(), anyLong())).thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/board-kick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));

        verify(boardKickService, times(1)).save(any(BoardKick.class));
    }

    @Test
    @DisplayName("게시글 킥 생성 실패 - 게시글 NotFoundException")
    void createBoardKick_notFoundBoard() throws Exception {
        // given
        User mockUser = User.builder().pk(1L).build();

        CreateBoardKickRequest  request = new CreateBoardKickRequest();
        request.setBoard(999L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
        when(boardService.findByPk(anyLong())).thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/board-kick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_BOARD"));


        verify(boardKickService, never()).save(any());
    }

    @Test
    @DisplayName("게시글 킥 생성 성공 - 이미 Kick 있는 경우")
    void createBoardKick_alreadyExists_toggleStatus() throws Exception {
        // given
        User mockUser = User.builder().pk(1L).build();
        Board mockBoard = Board.builder().pk(20L).build();
        CreateBoardKickRequest  request = new CreateBoardKickRequest();
        request.setBoard(20L);
        BoardKick existingKick = BoardKick.builder()
                .pk(100L)
                .board(mockBoard)
                .user(mockUser)
                .status(DataStatus.ACTIVATED)
                .build();

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
        when(boardService.findByPk(anyLong())).thenReturn(mockBoard);
        when(boardKickService.findByBoardAndUser(anyLong(), anyLong())).thenReturn(existingKick);

        // when & then
        mockMvc.perform(post("/api/board-kick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));

        verify(boardKickService, times(1)).save(existingKick);
        assertThat(existingKick.getStatus()).isEqualTo(DataStatus.DEACTIVATED);
    }
}
