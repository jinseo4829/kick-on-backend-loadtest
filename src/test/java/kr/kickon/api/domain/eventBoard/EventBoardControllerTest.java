package kr.kickon.api.domain.eventBoard;

import kr.kickon.api.domain.eventBoard.dto.GetEventBoardDTO;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventBoardControllerTest {

    @InjectMocks
    private EventBoardController eventBoardController;

    @Mock
    private EventBoardService eventBoardService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setPk(1L);
    }

    @Test
    @DisplayName("배너 게시글 리스트 조회 - 성공")
    void getEventBoards_success() {
        // given
        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);

        GetEventBoardDTO board1 = new GetEventBoardDTO();
        board1.setPk(1L);
        board1.setTitle("Event 1");

        GetEventBoardDTO board2 = new GetEventBoardDTO();
        board2.setPk(2L);
        board2.setTitle("Event 2");

        when(eventBoardService.findAll()).thenReturn(List.of(board1, board2));

        // when
        ResponseEntity<ResponseDTO<List<GetEventBoardDTO>>> response =
                eventBoardController.getEventBoards();

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("GET_SUCCESS");
        assertThat(response.getBody().getData()).hasSize(2);

        GetEventBoardDTO dto1 = response.getBody().getData().get(0);
        assertThat(dto1.getPk()).isEqualTo(1L);
        assertThat(dto1.getTitle()).isEqualTo("Event 1");

        GetEventBoardDTO dto2 = response.getBody().getData().get(1);
        assertThat(dto2.getPk()).isEqualTo(2L);
        assertThat(dto2.getTitle()).isEqualTo("Event 2");

        verify(eventBoardService, times(1)).findAll();
    }

    @Test
    @DisplayName("배너 게시글 리스트 조회 - 빈 리스트")
    void getEventBoards_emptyList() {
        // given
        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(eventBoardService.findAll()).thenReturn(List.of());

        // when
        ResponseEntity<ResponseDTO<List<GetEventBoardDTO>>> response =
                eventBoardController.getEventBoards();

        // then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEmpty();

        verify(eventBoardService, times(1)).findAll();
    }
}
