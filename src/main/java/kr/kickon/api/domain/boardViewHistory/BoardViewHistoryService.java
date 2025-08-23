package kr.kickon.api.domain.boardViewHistory;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.BoardViewHistory;
import kr.kickon.api.global.common.entities.QBoardViewHistory;
import kr.kickon.api.global.common.enums.DataStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardViewHistoryService {
    private final BoardViewHistoryRepository boardViewHistoryRepository;

    // region {findByPk} PK 기반 게시글 조회 기록 조회
    /**
     * PK를 기반으로 활성 상태의 게시글 조회 기록을 조회합니다.
     *
     * @param pk 게시글 조회 기록의 PK
     * @return BoardViewHistory 엔티티 (없으면 null)
     */
    public BoardViewHistory findByPk(Long pk) {
        BooleanExpression predicate = QBoardViewHistory.boardViewHistory.pk.eq(pk).and(QBoardViewHistory.boardViewHistory.status.eq(DataStatus.ACTIVATED));
        Optional<BoardViewHistory> boardViewHistory = boardViewHistoryRepository.findOne(predicate);
        return boardViewHistory.orElse(null);
    }
    // endregion

    // region {save} 게시글 조회 기록 저장
    /**
     * 게시글 조회 기록을 저장합니다.
     *
     * @param boardViewHistory 저장할 BoardViewHistory 엔티티
     */
    public void save(BoardViewHistory boardViewHistory) {
        boardViewHistoryRepository.save(boardViewHistory);
    }
    // endregion

    // region 게시글 조회수 반환
    /**
     * Board PK로 조회수 계산
     *
     * @param board 게시글 엔티티
     * @return Long 조회수
     */
    public Long countViewsByBoardPk(Board board) {
        return boardViewHistoryRepository.countByBoard_Pk(board.getPk());
    }
    // endregion
}
