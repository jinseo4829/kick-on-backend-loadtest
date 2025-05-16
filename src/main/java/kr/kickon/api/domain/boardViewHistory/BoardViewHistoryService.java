package kr.kickon.api.domain.boardViewHistory;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.BoardViewHistory;
import kr.kickon.api.global.common.entities.QBoardViewHistory;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardViewHistoryService implements BaseService<BoardViewHistory> {
    private final BoardViewHistoryRepository boardViewHistoryRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public BoardViewHistory findById(String uuid) {
        BooleanExpression predicate = QBoardViewHistory.boardViewHistory.id.eq(uuid).and(QBoardViewHistory.boardViewHistory.status.eq(DataStatus.ACTIVATED));
        Optional<BoardViewHistory> boardViewHistory = boardViewHistoryRepository.findOne(predicate);
        return boardViewHistory.orElse(null);
    }

    @Override
    public BoardViewHistory findByPk(Long pk) {
        BooleanExpression predicate = QBoardViewHistory.boardViewHistory.pk.eq(pk).and(QBoardViewHistory.boardViewHistory.status.eq(DataStatus.ACTIVATED));
        Optional<BoardViewHistory> boardViewHistory = boardViewHistoryRepository.findOne(predicate);
        return boardViewHistory.orElse(null);
    }

    public void save(BoardViewHistory boardViewHistory) {
        boardViewHistoryRepository.save(boardViewHistory);
    }
}
