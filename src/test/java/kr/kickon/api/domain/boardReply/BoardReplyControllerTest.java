package kr.kickon.api.domain.boardReply;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.boardReply.dto.PaginatedReplyListDTO;
import kr.kickon.api.domain.boardReply.dto.ReplyDTO;
import kr.kickon.api.domain.boardReply.request.CreateBoardReplyRequest;
import kr.kickon.api.domain.boardReply.request.PatchBoardReplyRequest;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.entities.*;
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

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BoardReplyControllerTest {


    @InjectMocks
    private BoardReplyController boardReplyController;

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserFavoriteTeamService userFavoriteTeamService;
    @Mock
    private BoardReplyService boardReplyService;
    @Mock
    private BoardService boardService;
    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(boardReplyController)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper()))
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void createBoardReply_success() throws Exception {
        // given
        User user = User.builder().pk(1L).id("user1").build();
        Board board = Board.builder().pk(10L).team(Team.builder().pk(100L).build()).build();
        CreateBoardReplyRequest request = new CreateBoardReplyRequest();
        request.setBoard(10L);
        request.setContents("hello");

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(userService.findByPk(1L)).thenReturn(user);
        when(boardService.findByPk(10L)).thenReturn(board);
        when(userFavoriteTeamService.findAllByUserPk(1L))
                .thenReturn(List.of(UserFavoriteTeam.builder().team(board.getTeam()).build()));
        when(boardReplyService.createBoardReplyWithImages(any(), any())).thenAnswer(inv -> inv.getArgument(0));

        // when & then
        mockMvc.perform(post("/api/board-reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));

        verify(boardReplyService).createBoardReplyWithImages(any(BoardReply.class), any());
        verify(boardReplyService).sendReplyNotification(eq(board), isNull(), eq(user));
    }

    @Test
    @DisplayName("댓글 생성 실패 - 게시글 없음")
    void createBoardReply_notFoundBoard() throws Exception {
        User user = User.builder().pk(1L).build();
        CreateBoardReplyRequest request = new CreateBoardReplyRequest();
        request.setBoard(999L);
        request.setContents("hello");

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(userService.findByPk(1L)).thenReturn(user);
        when(boardService.findByPk(999L)).thenReturn(null);

        mockMvc.perform(post("/api/board-reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_BOARD"));

        verify(boardReplyService, never()).createBoardReplyWithImages(any(), any());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteBoardReply_success() throws Exception {
        User user = User.builder().pk(1L).id("user1").build();
        BoardReply reply = BoardReply.builder().pk(100L).user(user).contents("hi").build();

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(boardReplyService.findByPk(100L)).thenReturn(reply);

        mockMvc.perform(delete("/api/board-reply/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));

        verify(boardReplyService).deleteBoardReply(reply);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 다른 유저")
    void deleteBoardReply_forbidden() throws Exception {
        User user = User.builder().pk(1L).id("user1").build();
        User otherUser = User.builder().pk(2L).id("user2").build();
        BoardReply reply = BoardReply.builder().pk(100L).user(otherUser).contents("hi").build();

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(boardReplyService.findByPk(100L)).thenReturn(reply);

        mockMvc.perform(delete("/api/board-reply/100"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        verify(boardReplyService, never()).deleteBoardReply(any());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void patchBoardReply_success() throws Exception {
        User user = User.builder().pk(1L).id("user1").build();
        BoardReply reply = BoardReply.builder().pk(100L).user(user).contents("old").build();

        PatchBoardReplyRequest request = new PatchBoardReplyRequest();
        request.setContents("updated");

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(boardReplyService.findByPk(100L)).thenReturn(reply);

        mockMvc.perform(patch("/api/board-reply/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));

        verify(boardReplyService).updateBoardReply(reply);
        assertThat(reply.getContents()).isEqualTo("updated");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 다른 유저")
    void patchBoardReply_forbidden() throws Exception {
        User user = User.builder().pk(1L).id("user1").build();
        User otherUser = User.builder().pk(2L).id("user2").build();
        BoardReply reply = BoardReply.builder().pk(100L).user(otherUser).contents("old").build();

        PatchBoardReplyRequest request = new PatchBoardReplyRequest();
        request.setContents("updated");

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(boardReplyService.findByPk(100L)).thenReturn(reply);

        mockMvc.perform(patch("/api/board-reply/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        verify(boardReplyService, never()).updateBoardReply(any());
    }

        @Test
        @DisplayName("댓글 리스트 조회 성공")
        void getBoardReplies_success() throws Exception {
            // given
            User mockUser = User.builder().pk(1L).id("user1").build();
            Board mockBoard = Board.builder().pk(100L).build();
            BaseUserDTO mockUserDto = BaseUserDTO.builder()
                    .id("user1")
                    .nickname("테스트 유저")
                    .profileImageUrl("https://example.com/profile.png")
                    .isReporter(false)
                    .build();

            List<ReplyDTO> replies = List.of(
                    new ReplyDTO(1L, "댓글1", mockUserDto, null, false, null, null),
                    new ReplyDTO(2L, "댓글2", mockUserDto, null, false, null, null)
            );

            PaginatedReplyListDTO paginatedReplyListDTO = new PaginatedReplyListDTO();
            paginatedReplyListDTO.setCurrentPage(1);
            paginatedReplyListDTO.setPageSize(10);
            paginatedReplyListDTO.setTotalItems(100L);
            paginatedReplyListDTO.setHasNext(null);
            paginatedReplyListDTO.setReplyList(replies);

            when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
            when(boardService.findByPk(100L)).thenReturn(mockBoard);
            when(boardReplyService.getReplyListByBoard(eq(100L), eq(1L), anyInt(), anyInt(), any(), any()))
                    .thenReturn(paginatedReplyListDTO);

            // when & then
            mockMvc.perform(get("/api/board-reply")
                            .param("board", "100")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                    .andExpect(jsonPath("$.data[0].contents").value("댓글1"))
                    .andExpect(jsonPath("$.data[1].contents").value("댓글2"));
        }

        @Test
        @DisplayName("댓글 리스트 조회 실패 - 게시글 없음")
        void getBoardReplies_notFoundBoard() throws Exception {
            // given
            User mockUser = User.builder().pk(1L).id("user1").build();

            when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
            when(boardService.findByPk(999L)).thenReturn(null);

            // when & then
            mockMvc.perform(get("/api/board-reply")
                            .param("board", "999")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND_BOARD"));
        }
    }