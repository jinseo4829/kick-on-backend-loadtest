package kr.kickon.api.domain.board;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.teamReporter.TeamReporterService;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Spy
    @InjectMocks
    private BoardService boardService; // findByPk 메서드가 있는 서비스

    @Mock
    private BoardRepository boardRepository; // Repository Mock

    @Mock
    private TeamReporterService teamReporterService;

    @Mock
    private JPAQuery<Tuple> mockQuery;

    private Tuple mockTuple1;
    private Tuple mockTuple2;
    private BoardListDTO mockBoard;

    @BeforeEach
    void setup() {
        // 서로 다른 Tuple mock
        mockTuple1 = mock(Tuple.class);
        mockTuple2 = mock(Tuple.class);

        mockBoard = createMockBoard();
    }

    @Test
    @DisplayName("PK로 게시글 조회 성공")
    void findByPk() {
        // given
        Long pk = 1L;
        Board mockBoard = Board.builder()
                .pk(pk)
                .title("테스트 글")
                .status(DataStatus.ACTIVATED)
                .build();

        when(boardRepository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.of(mockBoard));

        // when
        Board result = boardService.findByPk(pk);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(pk);
        assertThat(result.getTitle()).isEqualTo("테스트 글");
    }

    @Test
    void createBoardWithMedia() {
    }

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
    @DisplayName("최신 게시글 10개 조회")
    void getTop10BoardList_success() {
        // given
        List<Tuple> mockTupleList = List.of(mockTuple1, mockTuple2);

        // 2. Mock the behavior of internal and chained methods
        // Mock the helper method to return our mockQuery
        doReturn(mockQuery).when(boardService).createBoardListDTOQuery();

        // Mock the entire QueryDSL chain
        given(mockQuery.groupBy(any(Expression.class), any(Expression.class))).willReturn(mockQuery);
        given(mockQuery.orderBy(any(OrderSpecifier.class))).willReturn(mockQuery);
        given(mockQuery.limit(10)).willReturn(mockQuery);
        given(mockQuery.fetch()).willReturn(mockTupleList); // Define the final result of the query

        // Mock the DTO mapping method
        doReturn(mockBoard).when(boardService).tupleToBoardListDTO(any(Tuple.class));

        // when
        List<BoardListDTO> result = boardService.getTop10BoardList();

        // then
        // Verify the final list is correct
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .allSatisfy(board -> assertThat(board).isEqualTo(mockBoard));

        // Verify
        verify(mockQuery).limit(10);
        verify(mockQuery).fetch();
        verify(boardService, times(2)).tupleToBoardListDTO(any(Tuple.class));
    }

    @Test
    @DisplayName("최신 게시글 10개 조회 실패 - 빈 리스트 반환")
    void getTop10BoardList_empty() {
        // given: createBoardListDTOQuery() 호출 시 Mock 반환
        doReturn(mockQuery).when(boardService).createBoardListDTOQuery();

        // QueryDSL 체인 mocking
        given(mockQuery.groupBy(any(Expression.class), any(Expression.class))).willReturn(mockQuery);
        given(mockQuery.orderBy(any(OrderSpecifier.class))).willReturn(mockQuery);
        given(mockQuery.limit(10)).willReturn(mockQuery);
        given(mockQuery.fetch()).willReturn(Collections.emptyList());

        // DTO 변환 메서드가 호출되지 않도록 설정

        // when
        List<BoardListDTO> result = boardService.getTop10BoardList();

        // then
        assertThat(result).isNotNull()
                .isEmpty(); // 반환 리스트가 빈 리스트인지 검증

        // verify: tupleToBoardListDTO 호출되지 않음
        verify(mockQuery).limit(10);
        verify(mockQuery).fetch();
    }

    private BoardListDTO createMockBoard() {

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

        return BoardListDTO.builder()
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
    }

    @Test
    void getBoardDetailDTOByPk() {
    }

    @Test
    void getBoardListWithPagination() {
    }

    @Test
    void save() {
    }

    @Test
    void deleteBoard() {
    }

    @Test
    void updateBoard() {
    }
}