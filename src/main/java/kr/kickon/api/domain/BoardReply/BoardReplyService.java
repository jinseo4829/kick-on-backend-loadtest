package kr.kickon.api.domain.BoardReply;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.BoardReply;
import kr.kickon.api.global.common.entities.QBoardReply;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardReplyService implements BaseService<BoardReply> {
    private final BoardReplyRepository boardReplyRepository;
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

    public void save(BoardReply boardReply) {
        boardReplyRepository.save(boardReply);
    }
}
