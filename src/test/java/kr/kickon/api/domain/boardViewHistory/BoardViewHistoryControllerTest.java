package kr.kickon.api.domain.boardViewHistory;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.boardViewHistory.request.CreateBoardViewHistoryRequest;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.BoardViewHistory;
import kr.kickon.api.global.common.entities.User;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class BoardViewHistoryControllerTest {

    @InjectMocks
    private BoardViewHistoryController controller;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private BoardViewHistoryService boardViewHistoryService;

    @Mock
    private BoardService boardService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper()))
                .build();
    }

    @Test
    @DisplayName("게시글 뷰 생성 성공 - 로그인 유저")
    void createBoardView_success_withUser() throws Exception {
        User mockUser = User.builder().pk(1L).build();
        Board mockBoard = Board.builder().pk(10L).build();

        CreateBoardViewHistoryRequest request = new CreateBoardViewHistoryRequest();
        request.setBoard(10L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
        when(boardService.findByPk(10L)).thenReturn(mockBoard);
        doNothing().when(boardViewHistoryService).save(any(BoardViewHistory.class));

        mockMvc.perform(post("/api/board-view-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));

        verify(boardViewHistoryService).save(argThat(bvh ->
                bvh.getBoard().getPk().equals(10L) &&
                        bvh.getUser() != null &&
                        bvh.getUser().getPk().equals(1L)
        ));
    }

    @Test
    @DisplayName("게시글 뷰 생성 성공 - 비회원")
    void createBoardView_success_guest() throws Exception {
        Board mockBoard = Board.builder().pk(10L).build();

        CreateBoardViewHistoryRequest request = new CreateBoardViewHistoryRequest();
        request.setBoard(10L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(null);
        when(boardService.findByPk(10L)).thenReturn(mockBoard);
        doNothing().when(boardViewHistoryService).save(any(BoardViewHistory.class));

        mockMvc.perform(post("/api/board-view-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));

        verify(boardViewHistoryService).save(argThat(bvh ->
                bvh.getBoard().getPk().equals(10L) &&
                        bvh.getUser() == null
        ));
    }

    @Test
    @DisplayName("게시글 뷰 생성 실패 - 게시글 NotFoundException")
    void createBoardView_notFoundBoard() throws Exception {
        CreateBoardViewHistoryRequest request = new CreateBoardViewHistoryRequest();
        request.setBoard(999L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(null);
        when(boardService.findByPk(999L)).thenReturn(null);

        mockMvc.perform(post("/api/board-view-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_BOARD"));

        verify(boardViewHistoryService, never()).save(any());
    }
}