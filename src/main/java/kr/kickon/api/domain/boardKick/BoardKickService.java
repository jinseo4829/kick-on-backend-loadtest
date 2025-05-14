package kr.kickon.api.domain.boardKick;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardKickService implements BaseService<BoardKick> {
    private final BoardKickRepository boardKickRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public BoardKick findById(String uuid) {
        BooleanExpression predicate = QBoardKick.boardKick.id.eq(uuid).and(QBoardKick.boardKick.status.eq(DataStatus.ACTIVATED));
        Optional<BoardKick> boardKick = boardKickRepository.findOne(predicate);
        return boardKick.orElse(null);
    }

    @Override
    public BoardKick findByPk(Long pk) {
        BooleanExpression predicate = QBoardKick.boardKick.pk.eq(pk).and(QBoardKick.boardKick.status.eq(DataStatus.ACTIVATED));
        Optional<BoardKick> boardKick = boardKickRepository.findOne(predicate);
        return boardKick.orElse(null);
    }

    public BoardKick findByBoardAndUser(Long boardPk, Long userPk){
        BooleanExpression predicate = QBoardKick.boardKick.board.pk.eq(boardPk).and(QBoardKick.boardKick.user.pk.eq(userPk).and(QBoardKick.boardKick.status.eq(DataStatus.ACTIVATED)));
        Optional<BoardKick> boardKick = boardKickRepository.findOne(predicate);
        return boardKick.orElse(null);
    }

    public List<BoardKick> findByBoardPk(Long boardPk) {
        BooleanExpression predicate = QBoardKick.boardKick.board.pk.eq(boardPk).and(QBoardKick.boardKick.status.eq(DataStatus.ACTIVATED));
        return (List<BoardKick>) boardKickRepository.findAll(predicate);
    }

    public void save(BoardKick boardKick) {
        boardKickRepository.save(boardKick);
    }
}
