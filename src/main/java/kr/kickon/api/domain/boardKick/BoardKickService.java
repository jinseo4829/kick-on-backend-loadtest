package kr.kickon.api.domain.boardKick;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardKickService {
    private final BoardKickRepository boardKickRepository;

    // region {findByPk} BoardKick PK로 조회
    /**
     * BoardKick의 PK 기반 단건 조회
     */
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

    // region 게시글 킥 수 반환
    /**
     * Board PK로 킥 수 계산
     *
     * @param board 게시글 엔티티
     * @return Long 킥 수
     */
    public Long countKicksByBoardPk(Board board) {
        return boardKickRepository.countByBoard_PkAndStatus(board.getPk(), DataStatus.ACTIVATED);
    }
    // endregion
}
