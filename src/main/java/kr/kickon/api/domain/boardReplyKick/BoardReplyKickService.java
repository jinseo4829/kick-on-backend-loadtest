package kr.kickon.api.domain.boardReplyKick;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.BoardKick;
import kr.kickon.api.global.common.entities.BoardReplyKick;
import kr.kickon.api.global.common.entities.QBoardKick;
import kr.kickon.api.global.common.entities.QBoardReplyKick;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardReplyKickService implements BaseService<BoardReplyKick> {
    private final BoardReplyKickRepository boardReplyKickRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public BoardReplyKick findById(String uuid) {
        BooleanExpression predicate = QBoardReplyKick.boardReplyKick.id.eq(uuid).and(QBoardReplyKick.boardReplyKick.status.eq(DataStatus.ACTIVATED));
        Optional<BoardReplyKick> boardReplyKick = boardReplyKickRepository.findOne(predicate);
        return boardReplyKick.orElse(null);
    }

    @Override
    public BoardReplyKick findByPk(Long pk) {
        BooleanExpression predicate = QBoardReplyKick.boardReplyKick.pk.eq(pk).and(QBoardReplyKick.boardReplyKick.status.eq(DataStatus.ACTIVATED));
        Optional<BoardReplyKick> boardReplyKick = boardReplyKickRepository.findOne(predicate);
        return boardReplyKick.orElse(null);
    }

    public BoardReplyKick findByBoardReplyAndUser(Long boardReplyPk, Long userPk){
        BooleanExpression predicate = QBoardReplyKick.boardReplyKick.boardReply.pk.eq(boardReplyPk).and(QBoardReplyKick.boardReplyKick.status.eq(DataStatus.ACTIVATED).and(QBoardReplyKick.boardReplyKick.user.pk.eq(userPk)));
        Optional<BoardReplyKick> boardReplyKick = boardReplyKickRepository.findOne(predicate);
        return boardReplyKick.orElse(null);
    }

    public Long countKicks(Long boardReplyPk) {
        // 조건에 맞는 BoardReplyKick의 개수를 반환
        QBoardReplyKick boardReplyKick = QBoardReplyKick.boardReplyKick;
        Long num = queryFactory
                .select(boardReplyKick.countDistinct())
                .from(boardReplyKick)
                .where(boardReplyKick.boardReply.pk.eq(boardReplyPk)
                        .and(boardReplyKick.status.eq(DataStatus.ACTIVATED))
                        .and(boardReplyKick.user.status.eq(DataStatus.ACTIVATED)))
                .fetchOne();  // 해당 조건에 맞는 레코드 개수를 반환
        return  num;
    }
}
