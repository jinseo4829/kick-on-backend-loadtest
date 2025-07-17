package kr.kickon.api.domain.boardKick;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

    // region {findById} BoardKick ID로 조회
    /**
     * BoardKick의 UUID 기반 단건 조회
     */
    @Override
    public BoardKick findById(String uuid) {
        BooleanExpression predicate = QBoardKick.boardKick.id.eq(uuid).and(QBoardKick.boardKick.status.eq(DataStatus.ACTIVATED));
        Optional<BoardKick> boardKick = boardKickRepository.findOne(predicate);
        return boardKick.orElse(null);
    }
    //#endregion

    // region {findByPk} BoardKick PK로 조회
    /**
     * BoardKick의 PK 기반 단건 조회
     */
    @Override
    public BoardKick findByPk(Long pk) {
        BooleanExpression predicate = QBoardKick.boardKick.pk.eq(pk).and(QBoardKick.boardKick.status.eq(DataStatus.ACTIVATED));
        Optional<BoardKick> boardKick = boardKickRepository.findOne(predicate);
        return boardKick.orElse(null);
    }
    // endregion

    // region {findByBoardAndUser} 특정 게시글과 유저에 대한 BoardKick 조회
    /**
     * 게시글과 유저에 대한 BoardKick 존재 여부 조회
     */
    public BoardKick findByBoardAndUser(Long boardPk, Long userPk){
        BooleanExpression predicate = QBoardKick.boardKick.board.pk.eq(boardPk).and(QBoardKick.boardKick.user.pk.eq(userPk).and(QBoardKick.boardKick.status.eq(DataStatus.ACTIVATED)));
        Optional<BoardKick> boardKickEntity = boardKickRepository.findOne(predicate);
        return boardKickEntity.orElse(null);
    }
    // endregion

    // region {save} BoardKick 저장
    /**
     * BoardKick 저장 메서드
     */
    public void save(BoardKick boardKick) {
        boardKickRepository.save(boardKick);
    }
    // endregion
}
