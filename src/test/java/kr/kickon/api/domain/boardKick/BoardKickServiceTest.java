package kr.kickon.api.domain.boardKick;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.BoardKick;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardKickServiceTest {

    @InjectMocks
    private BoardKickService boardKickService;

    @Mock
    private BoardKickRepository boardKickRepository;

    @Test
    @DisplayName("findByPk 성공 - BoardKick 반환")
    void findByPk_success() {
        // given
        BoardKick boardKick = BoardKick.builder().pk(1L).status(DataStatus.ACTIVATED).build();
        when(boardKickRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(boardKick));

        // when
        BoardKick result = boardKickService.findByPk(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByPk 실패 - BoardKick 없음")
    void findByPk_notFound() {
        // given
        when(boardKickRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.empty());

        // when
        BoardKick result = boardKickService.findByPk(999L);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("findByBoardAndUser 성공 - BoardKick 반환")
    void findByBoardAndUser_success() {
        // given
        Board board = Board.builder().pk(10L).build();
        User user = User.builder().pk(20L).build();
        BoardKick boardKick = BoardKick.builder()
                .pk(100L)
                .board(board)
                .user(user)
                .status(DataStatus.ACTIVATED)
                .build();

        when(boardKickRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(boardKick));

        // when
        BoardKick result = boardKickService.findByBoardAndUser(10L, 20L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBoard().getPk()).isEqualTo(10L);
        assertThat(result.getUser().getPk()).isEqualTo(20L);
    }

    @Test
    @DisplayName("findByBoardAndUser 실패 - BoardKick 없음")
    void findByBoardAndUser_notFound() {
        // given
        when(boardKickRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.empty());

        // when
        BoardKick result = boardKickService.findByBoardAndUser(10L, 20L);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("save 성공 - BoardKick 저장 호출")
    void save_success() {
        // given
        Board board = Board.builder().pk(1L).build();
        User user = User.builder().pk(2L).build();
        BoardKick boardKick = BoardKick.builder().board(board).user(user).build();

        when(boardKickRepository.save(any())).thenReturn(boardKick);

        // when
        boardKickService.save(boardKick);

        // then
        verify(boardKickRepository, times(1)).save(boardKick);
    }
}