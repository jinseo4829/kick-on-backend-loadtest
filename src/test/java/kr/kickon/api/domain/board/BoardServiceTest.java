package kr.kickon.api.domain.board;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.board.dto.BoardDetailDTO;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.PaginatedBoardListDTO;
import kr.kickon.api.domain.boardKick.BoardKickService;
import kr.kickon.api.domain.embeddedLink.EmbeddedLinkService;
import kr.kickon.api.domain.partners.PartnersService;
import kr.kickon.api.domain.shorts.ShortsService;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.teamReporter.TeamReporterService;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ShortsType;
import kr.kickon.api.global.common.enums.UsedInType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private BoardKickService boardKickService;

    @Mock
    private PartnersService partnersService;

    @Mock
    private AwsFileReferenceService awsFileReferenceService;

    @Mock
    private EmbeddedLinkService embeddedLinkService;

    @Mock
    private ShortsService shortsService;

    @Mock
    private AwsService awsService;

    @Mock
    private JPAQuery<Tuple> mockQuery;

    @Mock
    private JPAQueryFactory queryFactory;

    private Tuple mockTuple1;
    private Tuple mockTuple2;
    private BoardListDTO mockBoard;

    @BeforeEach
    void setup() {
        // 서로 다른 Tuple mock
        mockTuple1 = mock(Tuple.class);
        mockTuple2 = mock(Tuple.class);

        mockBoard = createMockBoard();
        ReflectionTestUtils.setField(boardService, "env", "test-prefix");
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
    @DisplayName("게시글 생성 성공")
    void createBoardWithMedia_success() {
        // given
        Board board = Board.builder().pk(1L).build();
        String[] usedImages = {"image1.jpg"};
        String[] usedVideos = {"video1.mp4"};
        String[] embeddedLinks = {"https://youtube.com"};

        when(boardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AwsFileReference videoFile = AwsFileReference.builder().pk(101L).s3Key("test-prefix/board-files/video1.mp4").build();
        when(awsFileReferenceService.findbyBoardPk(1L)).thenReturn(List.of(videoFile));

        doNothing().when(awsFileReferenceService).updateFilesAsUsed(anyList(), any(), anyLong());
        doNothing().when(shortsService).save(any(), anyLong());
        doNothing().when(embeddedLinkService).saveAll(anyList());

        // when
        Board created = boardService.createBoardWithMedia(board, usedImages, usedVideos, embeddedLinks);

        // then
        assertThat(created.getPk()).isEqualTo(1L);

        // 이미지 파일 등록
        verify(awsFileReferenceService).updateFilesAsUsed(
                argThat(list -> list.contains("test-prefix/board-files/image1.jpg")),
                eq(UsedInType.BOARD),
                eq(1L)
        );

        // 영상 파일 등록 + Shorts 저장
        verify(awsFileReferenceService).updateFilesAsUsed(
                argThat(list -> list.contains("test-prefix/board-files/video1.mp4")),
                eq(UsedInType.BOARD),
                eq(1L)
        );
        verify(shortsService).save(ShortsType.AWS_FILE, 101L);

        // 링크 등록 + Shorts 저장
        verify(embeddedLinkService).saveAll(argThat(list -> list.stream().anyMatch(l -> l.getUrl().equals("https://youtube.com"))));
        verify(shortsService).save(eq(ShortsType.AWS_FILE), anyLong());
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
    @DisplayName("게시글 상세 조회 성공")
    void getBoardDetailDTOByPk_success() {
        // given
        Long boardPk = 1L;
        User mockUser = User.builder().id("user1").pk(1L).build();
        User mockBoardOwner = User.builder().id("abcd").pk(1L).build();

        // 1. DTO 변환에 필요한 핵심 Tuple Mocking
        Tuple mockTuple = mock(Tuple.class);
        Board mockBoard = Board.builder()
                .pk(boardPk)
                .user(mockBoardOwner)
                .title("테스트 제목")
                .contents("테스트 내용")
                .hasImage(false)
                .isPinned(false)
                .createdAt(LocalDateTime.of(2025, 9, 1, 12, 0))
                .build();
        Team mockTeam = Team.builder().pk(1L).nameKr("FC 킥온").build();

        // Mock Tuple의 get() 메서드 설정
        when(mockTuple.get(QBoard.board)).thenReturn(mockBoard);
        when(mockTuple.get(QUser.user)).thenReturn(mockBoardOwner);
        when(mockTuple.get(QTeam.team)).thenReturn(mockTeam);
        when(mockTuple.get(anyInt(), eq(Long.class))).thenReturn(10L, 50L, 2L); // likes, views, replies 순서
        when(mockTuple.get(QBoard.board.createdAt)).thenReturn(mockBoard.getCreatedAt());

        // 2. Service 내부 메서드 Mocking
        doReturn(mockQuery).when(boardService).createBoardListDTOQuery();
        when(mockQuery.where(any(BooleanExpression.class))).thenReturn(mockQuery);
        when(mockQuery.groupBy(any(Expression.class))).thenReturn(mockQuery);
        when(mockQuery.fetchOne()).thenReturn(mockTuple);
        when(teamReporterService.findByUserId(anyString())).thenReturn(null);
        when(boardKickService.findByBoardAndUser(any(Long.class), any(Long.class))).thenReturn(null); // 수정된 부분
        when(partnersService.findByUserPk(any(Long.class))).thenReturn(false); // 수정된 부분
        when(awsFileReferenceService.findbyBoardPk(any(Long.class))).thenReturn(new ArrayList<>());
        when(embeddedLinkService.findByBoardPk(any(Long.class))).thenReturn(new ArrayList<>());

        // when
        BoardDetailDTO result = boardService.getBoardDetailDTOByPk(boardPk, mockUser);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(boardPk);
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
        assertThat(result.getUser().getId()).isEqualTo("abcd");
        assertThat(result.getTeam().getPk()).isEqualTo(1L);
        assertThat(result.getHasImage()).isFalse();
        assertThat(result.getIsKicked()).isFalse();
        assertThat(result.getIsInfluencer()).isFalse();
        assertThat(result.getUser().getIsReporter()).isFalse();
        assertThat(result.getUsedImageKeys()).isEmpty();
        assertThat(result.getEmbeddedLinks()).isEmpty();

        // verify
        verify(mockQuery, times(1)).fetchOne();
        verify(boardKickService, times(1)).findByBoardAndUser(any(Long.class), any(Long.class));
        verify(partnersService, times(1)).findByUserPk(any(Long.class));
        verify(teamReporterService, times(1)).findByUserId(anyString());
        verify(awsFileReferenceService, times(1)).findbyBoardPk(any(Long.class));
        verify(embeddedLinkService, times(1)).findByBoardPk(any(Long.class));
    }

    @Test
    @DisplayName("게시글 상세 조회 실패 - 게시글 없음")
    void getBoardDetailDTOByPk_notFound() {
        // given
        Long boardPk = 999L; // 존재하지 않는 PK
        User mockUser = User.builder().id("user1").build();

        doReturn(mockQuery).when(boardService).createBoardListDTOQuery();
        when(mockQuery.where(any(BooleanExpression.class))).thenReturn(mockQuery);
        when(mockQuery.groupBy(any(Expression.class))).thenReturn(mockQuery);
        when(mockQuery.fetchOne()).thenReturn(null); // fetchOne()이 null 반환

        // when
        BoardDetailDTO result = boardService.getBoardDetailDTOByPk(boardPk, mockUser);

        // then
        assertThat(result).isNull();

        // verify
        verify(mockQuery, times(1)).fetchOne();
        verify(boardKickService, never()).findByBoardAndUser(any(), any());
    }

    @Test
    @DisplayName("게시글 상세 조회 성공 - 파일 포함")
    void getBoardDetailDTOByPk_success_withAllDetails() {
        // given
        Long boardPk = 1L;
        User mockUser = User.builder().id("user1").pk(1L).build();
        User mockBoardOwner = User.builder().id("boardOwner1").pk(1L).build();

        // Tuple mocking
        Tuple mockTuple = mock(Tuple.class);
        Board mockBoard = Board.builder()
                .pk(boardPk)
                .user(mockBoardOwner)
                .title("테스트 제목")
                .contents("테스트 내용")
                .hasImage(true)
                .isPinned(true)
                .createdAt(LocalDateTime.of(2025, 9, 1, 12, 0))
                .build();
        Team mockTeam = Team.builder().pk(1L).nameKr("FC 킥온").build();

        when(mockTuple.get(QBoard.board)).thenReturn(mockBoard);
        when(mockTuple.get(QUser.user)).thenReturn(mockBoardOwner);
        when(mockTuple.get(QTeam.team)).thenReturn(mockTeam);
        when(mockTuple.get(anyInt(), eq(Long.class))).thenReturn(10L, 50L, 2L);
        when(mockTuple.get(QBoard.board.createdAt)).thenReturn(mockBoard.getCreatedAt());

        // 2. Service 내부 메서드 Mocking
        doReturn(mockQuery).when(boardService).createBoardListDTOQuery();
        when(mockQuery.where(any(BooleanExpression.class))).thenReturn(mockQuery);
        when(mockQuery.groupBy(any(Expression.class))).thenReturn(mockQuery);
        when(mockQuery.fetchOne()).thenReturn(mockTuple);
        when(teamReporterService.findByUserId(anyString())).thenReturn(new TeamReporter());
        when(boardKickService.findByBoardAndUser(any(Long.class), any(Long.class))).thenReturn(new BoardKick()); // 킥이 있는 경우
        when(partnersService.findByUserPk(any(Long.class))).thenReturn(true); // 인플루언서

        // 이미지, 링크 Mocking
        AwsFileReference mockImageRef = new AwsFileReference();
        mockImageRef.setS3Key("test-prefix/board-files/image1.jpg");
        when(awsFileReferenceService.findbyBoardPk(any(Long.class))).thenReturn(List.of(mockImageRef));

        EmbeddedLink mockLink = new EmbeddedLink();
        mockLink.setUrl("https://www.youtube.com/watch?v=mockvideo");
        when(embeddedLinkService.findByBoardPk(any(Long.class))).thenReturn(List.of(mockLink));

        // when
        BoardDetailDTO result = boardService.getBoardDetailDTOByPk(boardPk, mockUser);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(boardPk);
        assertThat(result.getTeam()).isNotNull();
        assertThat(result.getHasImage()).isTrue();
        assertThat(result.getIsPinned()).isTrue();
        assertThat(result.getIsKicked()).isTrue(); // 킥 여부 검증
        assertThat(result.getIsInfluencer()).isTrue(); // 인플루언서 여부 검증
        assertThat(result.getUser().getIsReporter()).isTrue(); // 기자 여부 검증
        assertThat(result.getUsedImageKeys()).hasSize(1);
        assertThat(result.getUsedImageKeys()[0]).isEqualTo("image1.jpg");
        assertThat(result.getEmbeddedLinks()).hasSize(1);
        assertThat(result.getEmbeddedLinks()[0]).isEqualTo("https://www.youtube.com/watch?v=mockvideo");

        // verify
        verify(mockQuery, times(1)).fetchOne();
        verify(boardKickService, times(1)).findByBoardAndUser(any(Long.class), any(Long.class));
        verify(partnersService, times(1)).findByUserPk(any(Long.class));
        verify(teamReporterService, times(1)).findByUserId(anyString());
        verify(awsFileReferenceService, times(1)).findbyBoardPk(any(Long.class));
        verify(embeddedLinkService, times(1)).findByBoardPk(any(Long.class));
    }

    @Test
    @DisplayName("게시글 리스트 - 일반 페이지네이션 성공")
    void getBoardListWithPagination_success() {
        // given
        Long teamPk = null;
        Integer page = 1;
        Integer size = 10;
        String sortBy = "recent";
        Boolean infiniteFlag = false;
        Long lastBoardPk = null;
        Long lastViewCount = null;

        List<Tuple> mockTupleList = createMockTuples(10);
        Long totalCount = 100L;

        JPAQuery<Long> totalQuery = mock(JPAQuery.class);
        when(queryFactory.select(any(Expression.class))).thenReturn(totalQuery);
        when(totalQuery.from(any(EntityPath.class))).thenReturn(totalQuery);
        when(totalQuery.join(any(EntityPath.class))).thenReturn(totalQuery);
        when(totalQuery.on(any(Predicate.class))).thenReturn(totalQuery);
        when(totalQuery.where(any(BooleanExpression.class))).thenReturn(totalQuery);
        when(totalQuery.fetchOne()).thenReturn(totalCount);

        // dataQuery mocking
        doReturn(mockQuery).when(boardService).createBoardListDTOQuery();
        when(mockQuery.groupBy(any(Expression.class), any(Expression.class))).thenReturn(mockQuery);
        when(mockQuery.offset(any(Long.class))).thenReturn(mockQuery);
        when(mockQuery.limit(any(Long.class))).thenReturn(mockQuery);
        when(mockQuery.orderBy(any(OrderSpecifier.class), any(OrderSpecifier.class))).thenReturn(mockQuery);
        when(mockQuery.fetch()).thenReturn(mockTupleList);

        // DTO 변환 메서드 mocking
        doReturn(new BoardListDTO()).when(boardService).tupleToBoardListDTO(any(Tuple.class));

        // when
        PaginatedBoardListDTO result = boardService.getBoardListWithPagination(
                teamPk, page, size, sortBy, infiniteFlag, lastBoardPk, lastViewCount);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBoardList()).hasSize(size);
        assertThat(result.getTotalItems()).isEqualTo(totalCount);
        assertThat(result.getCurrentPage()).isEqualTo(page);
        assertThat(result.getPageSize()).isEqualTo(size);
        assertThat(result.getHasNext()).isNull();

        // verify
        verify(mockQuery, times(2)).offset(any(Long.class));
        verify(mockQuery, times(2)).limit(any(Long.class));
        verify(mockQuery).orderBy(any(OrderSpecifier.class), any(OrderSpecifier.class));
        verify(totalQuery, times(1)).fetchOne();
        verify(mockQuery, times(1)).fetch();
    }

    @Test
    @DisplayName("게시글 리스트 조회 성공 - 팀 검색 필터링 포함")
    void getBoardListWithPagination_byTeam_success() {
        // given
        Long teamPk = 1L;
        Integer page = 1;
        int size = 10;
        String sortBy = "recent";
        Boolean infiniteFlag = false;

        List<Tuple> mockTupleList = createMockTuples(10);
        Long totalCount = 50L;

        // totalQuery mocking
        JPAQuery<Long> totalQuery = mock(JPAQuery.class);
        when(queryFactory.select(any(Expression.class))).thenReturn(totalQuery);
        when(totalQuery.from(any(EntityPath.class))).thenReturn(totalQuery);
        when(totalQuery.join(any(EntityPath.class))).thenReturn(totalQuery);
        when(totalQuery.on(any(Predicate.class))).thenReturn(totalQuery);
        when(totalQuery.where(any(BooleanExpression.class))).thenReturn(totalQuery);
        when(totalQuery.fetchOne()).thenReturn(totalCount);

        // dataQuery mocking
        doReturn(mockQuery).when(boardService).createBoardListDTOQuery();
        when(mockQuery.groupBy(any(Expression.class), any(Expression.class))).thenReturn(mockQuery);
        when(mockQuery.where(any(BooleanExpression.class))).thenReturn(mockQuery); // 팀 조건 추가
        when(mockQuery.offset(any(Long.class))).thenReturn(mockQuery);
        when(mockQuery.limit(any(Long.class))).thenReturn(mockQuery);
        when(mockQuery.orderBy(any(OrderSpecifier.class), any(OrderSpecifier.class))).thenReturn(mockQuery);
        when(mockQuery.fetch()).thenReturn(mockTupleList);

        doReturn(new BoardListDTO()).when(boardService).tupleToBoardListDTO(any(Tuple.class));

        // when
        PaginatedBoardListDTO result = boardService.getBoardListWithPagination(
                teamPk, page, size, sortBy, infiniteFlag, null, null);

        // then
        assertThat(result.getBoardList()).hasSize(size);
        assertThat(result.getTotalItems()).isEqualTo(totalCount);

        // verify
        verify(totalQuery, times(2)).where(any(BooleanExpression.class)); // 기본 조건 + 팀 조건
        verify(mockQuery, times(1)).where(any(BooleanExpression.class)); // groupBy + 팀 조건
    }

    @Test
    @DisplayName("게시글 리스트 - 무한스크롤(최신순) hasNext가 true인 경우")
    void getBoardListWithPagination_infiniteScroll_hasNext_true() {
        // given
        Integer page = 1;
        int size = 10;
        Boolean infiniteFlag = true;

        List<Tuple> mockTupleList = createMockTuples(size + 1);
        Long totalCount = 100L;

        // totalQuery mocking
        JPAQuery<Long> totalQuery = mock(JPAQuery.class);
        when(queryFactory.select(any(Expression.class))).thenReturn(totalQuery);
        when(totalQuery.from(any(EntityPath.class))).thenReturn(totalQuery);
        when(totalQuery.join(any(EntityPath.class))).thenReturn(totalQuery);
        when(totalQuery.on(any(Predicate.class))).thenReturn(totalQuery);
        when(totalQuery.where(any(BooleanExpression.class))).thenReturn(totalQuery);
        when(totalQuery.fetchOne()).thenReturn(totalCount);

        // dataQuery mocking
        doReturn(mockQuery).when(boardService).createBoardListDTOQuery();
        when(mockQuery.groupBy(any(Expression.class), any(Expression.class))).thenReturn(mockQuery);
        when(mockQuery.offset(any(Long.class))).thenReturn(mockQuery);
        when(mockQuery.limit(any(Long.class))).thenReturn(mockQuery);
        when(mockQuery.orderBy(any(OrderSpecifier.class), any(OrderSpecifier.class))).thenReturn(mockQuery);
        when(mockQuery.fetch()).thenReturn(mockTupleList);

        doReturn(new BoardListDTO()).when(boardService).tupleToBoardListDTO(any(Tuple.class));

        // when
        PaginatedBoardListDTO result = boardService.getBoardListWithPagination(
                null, page, size, "recent", infiniteFlag, null, null);

        // then
        assertThat(result.getBoardList()).hasSize(size);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getTotalItems()).isNull(); // 무한스크롤 시 totalItems는 null
    }

    @Test
    @DisplayName("게시글 리스트 성공 - hot 게시글 정렬")
    void getBoardListWithPagination_hotSort_success() {
        // given
        Integer page = 1;
        int size = 10;
        String sortBy = "hot";
        Boolean infiniteFlag = false;

        List<Tuple> mockTupleList = createMockTuples(10);
        Long totalCount = 50L;

        // totalQuery mocking
        JPAQuery<Long> totalQuery = mock(JPAQuery.class);
        when(queryFactory.select(any(Expression.class))).thenReturn(totalQuery);
        when(totalQuery.from(any(EntityPath.class))).thenReturn(totalQuery);
        when(totalQuery.join(any(EntityPath.class))).thenReturn(totalQuery);
        when(totalQuery.on(any(Predicate.class))).thenReturn(totalQuery);
        when(totalQuery.where(any(BooleanExpression.class))).thenReturn(totalQuery);
        when(totalQuery.fetchOne()).thenReturn(totalCount);

        // dataQuery mocking
        doReturn(mockQuery).when(boardService).createBoardListDTOQuery();
        when(mockQuery.groupBy(any(Expression.class), any(Expression.class))).thenReturn(mockQuery);
        when(mockQuery.where(any(BooleanExpression.class))).thenReturn(mockQuery);
        when(mockQuery.orderBy(any(OrderSpecifier.class), any(OrderSpecifier.class))).thenReturn(mockQuery);
        when(mockQuery.offset(any(Long.class))).thenReturn(mockQuery);
        when(mockQuery.limit(any(Long.class))).thenReturn(mockQuery);
        when(mockQuery.fetch()).thenReturn(mockTupleList);

        doReturn(new BoardListDTO()).when(boardService).tupleToBoardListDTO(any(Tuple.class));

        // when
        PaginatedBoardListDTO result = boardService.getBoardListWithPagination(
                null, page, size, sortBy, infiniteFlag, null, null);

        // then
        assertThat(result.getBoardList()).hasSize(size);
        assertThat(result.getTotalItems()).isEqualTo(totalCount);

        // verify
        verify(mockQuery).orderBy(any(OrderSpecifier.class), any(OrderSpecifier.class));
        verify(mockQuery, times(1)).where(any(BooleanExpression.class));
    }

    private List<Tuple> createMockTuples(int count) {
        List<Tuple> tuples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Tuple mockTuple = mock(Tuple.class);
            tuples.add(mockTuple);
        }
        return tuples;
    }

    @Test
    @DisplayName("게시글 저장 성공")
    void save_success() {
        // given
        Board mockBoard = Board.builder()
                .title("테스트 게시글")
                .contents("테스트 내용")
                .build();

        when(boardRepository.save(any(Board.class))).thenReturn(mockBoard);

        // when
        Board savedBoard = boardService.save(mockBoard);

        // then
        assertThat(savedBoard).isNotNull();
        assertThat(savedBoard.getTitle()).isEqualTo("테스트 게시글");
        assertThat(savedBoard.getContents()).isEqualTo("테스트 내용");

        // verify
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deleteBoard_success() {
        // given
        Board mockBoard = Board.builder()
                .pk(1L)
                .build();

        AwsFileReference mockFile = new AwsFileReference();
        mockFile.setPk(100L);
        mockFile.setS3Key("test.png");
        List<AwsFileReference> fileList = List.of(mockFile);

        EmbeddedLink mockLink = new EmbeddedLink();
        mockLink.setPk(200L);
        mockLink.setUrl("https://youtube.com/test");
        List<EmbeddedLink> linkList = List.of(mockLink);

        when(awsFileReferenceService.findbyBoardPk(1L)).thenReturn(fileList);
        when(embeddedLinkService.findByBoardPk(1L)).thenReturn(linkList);

        doNothing().when(awsService).deleteFileFromS3AndDb(any(), any());
        doNothing().when(embeddedLinkService).deleteFileFromDb(mockLink);
        doNothing().when(shortsService).deleteByReferencePkAndType(any(), any());

        // when
        try (MockedStatic<S3Client> mocked = mockStatic(S3Client.class)) {
            S3Client mockS3 = mock(S3Client.class);
            S3ClientBuilder mockBuilder = mock(S3ClientBuilder.class);

            mocked.when(S3Client::builder).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockS3);

            boardService.deleteBoard(mockBoard);
        }

        // then
        verify(awsService, times(1))
                .deleteFileFromS3AndDb(any(), any());
        verify(shortsService, times(1))
                .deleteByReferencePkAndType(mockLink.getPk(), ShortsType.EMBEDDED_LINK);
        verify(embeddedLinkService, times(1)).deleteFileFromDb(mockLink);
        verify(boardRepository, times(1)).save(mockBoard);

        assertThat(mockBoard.getStatus()).isEqualTo(DataStatus.DEACTIVATED);
    }

    @Test
    @DisplayName("게시글 업데이트 - 기존 파일 삭제, 신규 파일/링크 추가")
    void updateBoard_success() {
        // given
        Board board = Board.builder().pk(1L).build();

        // 기존 파일/링크
        AwsFileReference oldImage = AwsFileReference.builder()
                .pk(100L)
                .s3Key("test-prefix/board-files/oldImage.jpg")
                .build();
        AwsFileReference oldVideo = AwsFileReference.builder()
                .pk(101L)
                .s3Key("test-prefix/board-files/oldVideo.mp4")
                .build();
        EmbeddedLink oldLink = EmbeddedLink.builder()
                .pk(200L)
                .url("https://old.com")
                .build();

        when(boardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(awsFileReferenceService.findbyBoardPk(1L)).thenReturn(List.of(oldImage, oldVideo));
        when(embeddedLinkService.findByBoardPk(1L)).thenReturn(List.of(oldLink));

        // 외부 호출 모두 doNothing
        doNothing().when(shortsService).deleteByReferencePkAndType(anyLong(), any());
        doNothing().when(awsService).deleteFileFromS3AndDb(any(), any());
        doNothing().when(awsFileReferenceService).updateFilesAsUsed(anyList(), any(), anyLong());
        doNothing().when(embeddedLinkService).deleteFileFromDb(any());

        // saveAll은 void 메소드이므로 doNothing()을 사용
        doNothing().when(embeddedLinkService).saveAll(anyList());

        String[] usedImageKeys = {"newImage.jpg"};
        String[] usedVideoKeys = {"newVideo.mp4"};
        String[] embeddedLinks = {"https://new.com"};

        doNothing().when(shortsService).save(any(), nullable(Long.class));


        // S3Client static mock
        try (MockedStatic<S3Client> s3Static = mockStatic(S3Client.class)) {
            S3Client mockS3 = mock(S3Client.class);
            S3ClientBuilder mockBuilder = mock(S3ClientBuilder.class);
            s3Static.when(S3Client::builder).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockS3);

            // when
            Board updated = boardService.updateBoard(board, usedImageKeys, usedVideoKeys, embeddedLinks);

            // then
            assertThat(updated.getPk()).isEqualTo(1L);

            // 기존 파일 삭제 호출 확인
            verify(shortsService).deleteByReferencePkAndType(100L, ShortsType.AWS_FILE);
            verify(awsService).deleteFileFromS3AndDb(any(), eq(oldImage));
            verify(shortsService).deleteByReferencePkAndType(101L, ShortsType.AWS_FILE);
            verify(awsService).deleteFileFromS3AndDb(any(), eq(oldVideo));

            // 신규 파일 등록 확인
            verify(awsFileReferenceService).updateFilesAsUsed(
                    argThat(list -> list.contains("test-prefix/board-files/newImage.jpg") &&
                            list.contains("test-prefix/board-files/newVideo.mp4")),
                    eq(UsedInType.BOARD),
                    eq(1L)
            );

            // 신규 링크 등록 확인
            verify(embeddedLinkService).saveAll(argThat(list ->
                    list.stream().anyMatch(l -> l.getUrl().equals("https://new.com"))));

            // shortsService.save()가 null을 허용하는지 검증
            verify(shortsService, atLeastOnce()).save(any(), nullable(Long.class));
        }
    }

}