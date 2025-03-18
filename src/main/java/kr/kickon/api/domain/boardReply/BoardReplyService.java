package kr.kickon.api.domain.boardReply;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.boardReply.dto.PaginatedReplyListDTO;
import kr.kickon.api.domain.boardReply.dto.ReplyDTO;
import kr.kickon.api.domain.board.dto.UserDTO;
import kr.kickon.api.domain.boardReplyKick.BoardReplyKickService;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardReplyService implements BaseService<BoardReply> {
    private final BoardReplyRepository boardReplyRepository;
    private final BoardReplyKickService boardReplyKickService;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public BoardReply findById(String uuid) {
        BooleanExpression predicate = QBoardReply.boardReply.id.eq(uuid).and(QBoardReply.boardReply.status.eq(DataStatus.ACTIVATED));
        Optional<BoardReply> boardReply = boardReplyRepository.findOne(predicate);
        return boardReply.orElse(null);
    }

    @Override
    public BoardReply findByPk(Long pk) {
        BooleanExpression predicate = QBoardReply.boardReply.pk.eq(pk).and(QBoardReply.boardReply.status.eq(DataStatus.ACTIVATED));
        Optional<BoardReply> boardReply = boardReplyRepository.findOne(predicate);
        return boardReply.orElse(null);
    }

    public PaginatedReplyListDTO getRepliesByBoard(Long boardPk, Long userPk, Integer page, Integer size) {
        QBoardReply reply = QBoardReply.boardReply;
        QUser user = QUser.user;
        Integer offset = (page - 1) * size;
        Long total = queryFactory.select(reply.pk.countDistinct())
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.board.pk.eq(boardPk)
                        .and(reply.parentBoardReply.isNull())
                        .and(user.status.eq(DataStatus.ACTIVATED))
                        .and(reply.status.eq(DataStatus.ACTIVATED)))
                .fetchOne();

        List<Tuple> results = queryFactory.select(reply, user)
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.board.pk.eq(boardPk)
                        .and(reply.parentBoardReply.isNull())
                        .and(user.status.eq(DataStatus.ACTIVATED))
                        .and(reply.status.eq(DataStatus.ACTIVATED)))
                .offset(offset)
                .limit(size)
                .orderBy(reply.createdAt.asc())
                .fetch();

        List<ReplyDTO> replyList = results.stream()
                .map(tuple -> mapToReplyDTO(tuple, userPk))
                .toList();
        return new PaginatedReplyListDTO(page, size, total, replyList);
    }

    private ReplyDTO mapToReplyDTO(Tuple tuple, Long userPk) {
        QBoardReply reply = QBoardReply.boardReply;
        QUser user = QUser.user;

        BoardReply parentReply = tuple.get(reply);
        User replyUser = tuple.get(user);
        BoardReplyKick myBoardReplyKick = null;
        if(userPk != null) {
            myBoardReplyKick = boardReplyKickService.findByBoardReplyAndUser(parentReply.getPk(), userPk);
        }
        Long kickCount = boardReplyKickService.countKicks(parentReply.getPk());
        return ReplyDTO.builder()
                .pk(parentReply.getPk())
                .contents(parentReply.getContents())
                .createdAt(parentReply.getCreatedAt())
                .user(UserDTO.builder()
                        .id(replyUser.getId())
                        .nickname(replyUser.getNickname())
                        .profileImageUrl(replyUser.getProfileImageUrl())
                        .build())
                .replies(parentReply.getParentBoardReply() != null ? null : getChildReplies(parentReply.getPk(), userPk))
                .isKicked(myBoardReplyKick!=null)
                .kickCount(kickCount)
                .build();
    }

    private List<ReplyDTO> getChildReplies(Long parentPk, Long userPk) {
        QBoardReply reply = QBoardReply.boardReply;
        QUser user = QUser.user;

        List<Tuple> results = queryFactory.select(reply, user)
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.parentBoardReply.pk.eq(parentPk)
                        .and(reply.status.eq(DataStatus.ACTIVATED)))
                .orderBy(reply.createdAt.asc())
                .fetch();

        return results.stream()
                .map(tuple -> mapToReplyDTO(tuple, userPk))
                .collect(Collectors.toList());
    }

    public void save(BoardReply boardReply) {
        boardReplyRepository.save(boardReply);
    }
}
