package kr.kickon.api.domain.board;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.UserDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService implements BaseService<Board> {
    private final BoardRepository boardRepository;
    private final JPAQueryFactory queryFactory;
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

    public List<BoardListDTO> findTop10Boards() {
        QBoard board = QBoard.board;
        QBoardKick boardKick = QBoardKick.boardKick;
        QBoardViewHistory boardViewHistory = QBoardViewHistory.boardViewHistory;
        QBoardReply boardReply = QBoardReply.boardReply;
        QUser user = QUser.user;

        List<Tuple> results = queryFactory.select(board, user,
                        boardKick.pk.count().coalesce(0L).as("kickCount"),
                        boardViewHistory.pk.count().coalesce(0L).as("viewCount"),
                        boardReply.pk.count().coalesce(0L).as("replyCount"))
                .from(board)
                .join(user).on(board.user.pk.eq(user.pk))
                .leftJoin(boardKick).on(board.pk.eq(boardKick.board.pk).and(boardKick.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(boardViewHistory).on(board.pk.eq(boardViewHistory.board.pk).and(boardViewHistory.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(boardReply).on(board.pk.eq(boardReply.board.pk).and(boardReply.status.eq(DataStatus.ACTIVATED)))
                .where(board.status.eq(DataStatus.ACTIVATED)
                        .and(user.status.eq(DataStatus.ACTIVATED)))
                .groupBy(board.pk, user.pk)
                .orderBy(board.createdAt.desc())
                .limit(10)
                .fetch();
        return results.stream().map(tuple -> {
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
                    .views(tuple.get(2, Long.class).intValue())
                    .likes(tuple.get(3, Long.class).intValue())
                    .replies(tuple.get(4, Long.class).intValue())
                    .build();
        }).toList();
    }
}
