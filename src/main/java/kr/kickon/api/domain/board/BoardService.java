package kr.kickon.api.domain.board;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.board.dto.BoardDetailDTO;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.PaginatedBoardListDTO;
import kr.kickon.api.domain.board.dto.UserDTO;
import kr.kickon.api.domain.boardKick.BoardKickService;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService implements BaseService<Board> {
    private final BoardRepository boardRepository;
    private final JPAQueryFactory queryFactory;
    private final BoardKickService boardKickService;
    private final UUIDGenerator uuidGenerator;

    @Override
    public Board findById(String uuid) {
        BooleanExpression predicate = QBoard.board.id.eq(uuid).and(QBoard.board.status.eq(DataStatus.ACTIVATED));
        Optional<Board> actualSeasonRanking = boardRepository.findOne(predicate);
        return actualSeasonRanking.orElse(null);
    }

    @Override
    public Board findByPk(Long pk) {
        BooleanExpression predicate = QBoard.board.pk.eq(pk).and(QBoard.board.status.eq(DataStatus.ACTIVATED));
        Optional<Board> actualSeasonRanking = boardRepository.findOne(predicate);
        return actualSeasonRanking.orElse(null);
    }

    public JPAQuery<Tuple> createBoardListDTOQuery() {
        QBoard board = QBoard.board;
        QBoardKick boardKick = QBoardKick.boardKick;
        QBoardViewHistory boardViewHistory = QBoardViewHistory.boardViewHistory;
        QBoardReply boardReply = QBoardReply.boardReply;
        QUser user = QUser.user;
        return queryFactory.select(board, user,
                        boardKick.pk.countDistinct().coalesce(0L).as("kickCount"),
                        boardViewHistory.pk.countDistinct().coalesce(0L).as("viewCount"),
                        boardReply.pk.countDistinct().coalesce(0L).as("replyCount"))
                .from(board)
                .join(user).on(board.user.pk.eq(user.pk))
                .leftJoin(boardKick).on(board.pk.eq(boardKick.board.pk).and(boardKick.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(boardViewHistory).on(board.pk.eq(boardViewHistory.board.pk).and(boardViewHistory.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(boardReply).on(board.pk.eq(boardReply.board.pk).and(boardReply.status.eq(DataStatus.ACTIVATED)))
                .where(board.status.eq(DataStatus.ACTIVATED)
                        .and(user.status.eq(DataStatus.ACTIVATED)));
    }
    public BoardListDTO tupleToBoardListDTO(Tuple tuple) {
        QBoard board = QBoard.board;
        QUser user = QUser.user;
        Board boardEntity = tuple.get(board);
        User userEntity = tuple.get(user);
        return BoardListDTO.builder()
                .pk(boardEntity.getPk())
                .title(boardEntity.getTitle())
                .user(UserDTO.builder()
                        .id(userEntity.getId())
                        .nickname(userEntity.getNickname())
                        .profileImageUrl(userEntity.getProfileImageUrl())
                        .build())
                .createdAt(tuple.get(board.createdAt))
                .createdAt(boardEntity.getCreatedAt())
                .likes(tuple.get(2, Long.class).intValue())
                .views(tuple.get(3, Long.class).intValue())
                .replies(tuple.get(4, Long.class).intValue())
                .build();
    }

    public List<BoardListDTO> findTop10Boards() {
        QBoard board = QBoard.board;
        QUser user = QUser.user;
        List<Tuple> results = createBoardListDTOQuery()
                .groupBy(board.pk, user.pk)
                .orderBy(board.createdAt.desc())
                .limit(10).fetch();
        return results.stream().map(this::tupleToBoardListDTO).toList();
    }

    public BoardDetailDTO findOneBoardListDTOByPk(Long boardPk,Long userPk) {
        QBoard board = QBoard.board;
        QUser user = QUser.user;
        Tuple result = createBoardListDTOQuery()
                .where(board.pk.eq(boardPk))
                .groupBy(board.pk, user.pk)
                .fetchOne();
        if(result == null) return null;

        BoardKick boardKick = boardKickService.findByBoardAndUser(result.get(board).getPk(),userPk);
        Board boardEntity = result.get(board);
        User userEntity = result.get(user);
        return BoardDetailDTO.builder()
                .pk(boardEntity.getPk())
                .title(boardEntity.getTitle())
                .user(UserDTO.builder()
                        .id(userEntity.getId())
                        .nickname(userEntity.getNickname())
                        .profileImageUrl(userEntity.getProfileImageUrl())
                        .build())
                .createdAt(result.get(board.createdAt))
                .createdAt(boardEntity.getCreatedAt())
                .likes(result.get(2, Long.class).intValue())
                .views(result.get(3, Long.class).intValue())
                .replies(result.get(4, Long.class).intValue())
                .isKicked(boardKick!=null)
                .content(boardEntity.getContents())
                .build();
    }

    public PaginatedBoardListDTO findBoardsWithPagination(Long teamPk, Integer page, Integer size, String sortBy) {
        QBoard board = QBoard.board;
        QBoardViewHistory boardViewHistory = QBoardViewHistory.boardViewHistory;
        QUser user = QUser.user;

        Integer offset = (page - 1) * size;
        LocalDateTime hotThreshold = LocalDateTime.now().minusHours(48); // 최근 48시간 기준

        JPAQuery<Long> totalQuery = queryFactory.select(board.pk.count())
                .from(board)
                .join(user).on(board.user.pk.eq(user.pk))
                .where(board.status.eq(DataStatus.ACTIVATED)
                        .and(user.status.eq(DataStatus.ACTIVATED)));
        if (teamPk != null) totalQuery.where(board.team.pk.eq(teamPk));
        if ("hot".equalsIgnoreCase(sortBy)) totalQuery.where(board.createdAt.goe(hotThreshold));

        Long totalCount = totalQuery.fetchOne();

        JPAQuery<Tuple> dataQuery = createBoardListDTOQuery()
                .groupBy(board.pk, user.pk)
                .offset(offset)
                .limit(size);
        if (teamPk != null) dataQuery.where(board.team.pk.eq(teamPk));

        if ("hot".equalsIgnoreCase(sortBy)) {
            dataQuery.where(board.createdAt.goe(hotThreshold)); // hot일 때 48시간 내 필터링
            dataQuery.orderBy(boardViewHistory.pk.count().coalesce(0L).desc(), board.createdAt.desc()); // 조회수 + 최신순
        } else {
            dataQuery.orderBy(board.createdAt.desc()); // 기본값: 최신순
        }
        List<Tuple> results = dataQuery.fetch();

        List<BoardListDTO> boardList = results.stream().map(this::tupleToBoardListDTO).toList();

        return new PaginatedBoardListDTO(page, size, totalCount, boardList);
    }

    public void save(Board board) {
        boardRepository.save(board);
    }
}
