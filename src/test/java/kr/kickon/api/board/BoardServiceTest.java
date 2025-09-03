package kr.kickon.api.board;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.teamReporter.TeamReporterService;
import kr.kickon.api.global.common.entities.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BoardServiceTest {

    @InjectMocks
    private BoardService boardService;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private JPAQuery<Tuple> jpaQuery;

    @Mock
    private Tuple tuple1;

    @Mock
    private Tuple tuple2;

    @Mock
    private TeamReporterService teamReporterService;

    @Test
    void tupleToBoardListDTO_success() {
        // given: Board, User, Team Mock Tuple 생성
        Board mockBoard = Board.builder()
                .pk(1L)
                .title("테스트 글")
                .hasImage(true)
                .isPinned(false)
                .createdAt(LocalDateTime.of(2025, 9, 1, 15, 0))
                .user(User.builder().id("user1").build())
                .build();

        User mockUser = User.builder()
                .id("user1")
                .nickname("테스트 유저")
                .profileImageUrl("https://example.com/profile.png")
                .build();

        Team mockTeam = Team.builder()
                .pk(1L)
                .nameKr("FC 킥온")
                .nameEn("FC Kickon")
                .logoUrl("https://example.com/logo.png")
                .build();

        Tuple mockTuple = mock(Tuple.class);
        when(mockTuple.get(QBoard.board)).thenReturn(mockBoard);
        when(mockTuple.get(QUser.user)).thenReturn(mockUser);
        when(mockTuple.get(QTeam.team)).thenReturn(mockTeam);
        when(mockTuple.get(QBoard.board.createdAt)).thenReturn(mockBoard.getCreatedAt());
        when(mockTuple.get(3, Long.class)).thenReturn(10L); // likes
        when(mockTuple.get(4, Long.class)).thenReturn(100L); // views
        when(mockTuple.get(5, Long.class)).thenReturn(5L);   // replies

        when(teamReporterService.findByUserId("user1")).thenReturn(null);

        // when
        BoardListDTO dto = boardService.tupleToBoardListDTO(mockTuple);

        // then
        assertThat(dto.getPk()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("테스트 글");
        assertThat(dto.getUser().getNickname()).isEqualTo("테스트 유저");
        assertThat(dto.getTeam().getNameEn()).isEqualTo("FC Kickon");
        assertThat(dto.getLikes()).isEqualTo(10);
        assertThat(dto.getViews()).isEqualTo(100);
        assertThat(dto.getReplies()).isEqualTo(5);
        assertThat(dto.getUser().getIsReporter()).isFalse();
    }

    @Test
    void getTop10BoardList_success() {
        // given
        List<Tuple> mockResults = List.of(tuple1, tuple2);
        when(boardService.createBoardListDTOQuery()).thenReturn(jpaQuery);
        when(jpaQuery.groupBy((Expression<?>) any())).thenReturn(jpaQuery);
        when(jpaQuery.orderBy((OrderSpecifier<?>) any())).thenReturn(jpaQuery);
        when(jpaQuery.limit(10L)).thenReturn(jpaQuery);
        when(jpaQuery.fetch()).thenReturn(mockResults);

        when(boardService.tupleToBoardListDTO(tuple1)).thenReturn(
                BoardListDTO.builder().pk(1L).title("글1").build()
        );
        when(boardService.tupleToBoardListDTO(tuple2)).thenReturn(
                BoardListDTO.builder().pk(2L).title("글2").build()
        );

        // when
        List<BoardListDTO> result = boardService.getTop10BoardList();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPk()).isEqualTo(1L);
        assertThat(result.get(1).getTitle()).isEqualTo("글2");
    }

    @Test
    void getTop10BoardList_failure() {
        when(boardService.createBoardListDTOQuery()).thenReturn(jpaQuery);
        when(jpaQuery.groupBy((Expression<?>) any())).thenReturn(jpaQuery);
        when(jpaQuery.orderBy((OrderSpecifier<?>) any())).thenReturn(jpaQuery);
        when(jpaQuery.limit(10L)).thenReturn(jpaQuery);
        when(jpaQuery.fetch()).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> boardService.getTop10BoardList())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }
}
