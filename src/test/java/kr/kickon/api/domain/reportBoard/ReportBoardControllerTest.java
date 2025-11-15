package kr.kickon.api.domain.reportBoard;

import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.reportBoard.request.CreateReportBoardRequestDTO;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportBoardControllerTest {

    @InjectMocks
    private ReportBoardController reportBoardController;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private BoardService boardService;

    @Mock
    private ReportBoardService reportBoardService;

    private User user;
    private Board board;

    @BeforeEach
    void setUp() {
        user = new User();
        board = new Board();
        board.setPk(1L);
    }

    @Test
    @DisplayName("게시글 신고 생성 - 성공")
    void createReportBoard_success() {
        // given
        CreateReportBoardRequestDTO request = new CreateReportBoardRequestDTO();
        request.setBoard(1L);
        request.setReason("욕설 및 비방");

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(boardService.findByPk(1L)).thenReturn(board);

        // when
        ResponseEntity<ResponseDTO<Void>> response =
                reportBoardController.createReportBoard(request);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("GET_SUCCESS");

        verify(reportBoardService, times(1)).save(any(ReportBoard.class));
    }

    @Test
    @DisplayName("게시글 신고 생성 - 실패 (게시글 없음)")
    void createReportBoard_fail_boardNotFound() {
        // given
        CreateReportBoardRequestDTO request = new CreateReportBoardRequestDTO();
        request.setBoard(999L);
        request.setReason("스팸");

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(boardService.findByPk(999L)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> reportBoardController.createReportBoard(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ResponseCode.NOT_FOUND_BOARD.getMessage());

        verify(reportBoardService, never()).save(any());
    }
}
