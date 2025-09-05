package kr.kickon.api.domain.boardReplyKick;

import kr.kickon.api.domain.boardReply.BoardReplyService;
import kr.kickon.api.domain.boardReplyKick.request.CreateBoardReplyKickRequest;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.error.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BoardReplyKickControllerTest {

    @InjectMocks
    private BoardReplyKickController controller;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private BoardReplyService boardReplyService;

    @Mock
    private BoardReplyKickService boardReplyKickService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper())) // NotFoundException 처리용
                .build();
    }

    @Test
    @DisplayName("댓글 킥 생성 성공 - 신규 킥")
    void createBoardReplyKick_success_newKick() throws Exception {
        User mockUser = User.builder().pk(1L).build();
        BoardReply reply = BoardReply.builder().pk(10L).build();

        CreateBoardReplyKickRequest request = new CreateBoardReplyKickRequest();
        request.setReply(10L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
        when(boardReplyService.findByPk(10L)).thenReturn(reply);
        when(boardReplyKickService.findByBoardReplyAndUser(10L, 1L)).thenReturn(null);

        mockMvc.perform(post("/api/board-reply-kick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));

        verify(boardReplyKickService).save(argThat(kick ->
                kick.getBoardReply().equals(reply) &&
                        kick.getUser().equals(mockUser)
        ));
    }

    @Test
    @DisplayName("댓글 킥 생성 성공 - 기존 킥 토글")
    void createBoardReplyKick_success_toggleKick() throws Exception {
        User mockUser = User.builder().pk(1L).build();
        BoardReply reply = BoardReply.builder().pk(10L).build();
        BoardReplyKick existingKick = BoardReplyKick.builder()
                .boardReply(reply)
                .user(mockUser)
                .status(DataStatus.ACTIVATED)
                .build();

        CreateBoardReplyKickRequest request = new CreateBoardReplyKickRequest();
        request.setReply(10L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
        when(boardReplyService.findByPk(10L)).thenReturn(reply);
        when(boardReplyKickService.findByBoardReplyAndUser(10L, 1L)).thenReturn(existingKick);

        mockMvc.perform(post("/api/board-reply-kick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));

        assertThat(existingKick.getStatus()).isEqualTo(DataStatus.DEACTIVATED);
        verify(boardReplyKickService).save(existingKick);
    }

    @Test
    @DisplayName("댓글 킥 생성 실패 - 댓글 NotFoundException")
    void createBoardReplyKick_notFoundReply() throws Exception {
        User mockUser = User.builder().pk(1L).build();

        CreateBoardReplyKickRequest request = new CreateBoardReplyKickRequest();
        request.setReply(999L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
        when(boardReplyService.findByPk(anyLong())).thenReturn(null);

        mockMvc.perform(post("/api/board-reply-kick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_BOARD_REPLY"));

        verify(boardReplyKickService, never()).save(any());
    }
}