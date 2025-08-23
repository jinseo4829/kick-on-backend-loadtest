package kr.kickon.api.domain.boardReplyKick;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.entities.BoardReplyKick;
import kr.kickon.api.global.common.entities.QBoardReplyKick;
import kr.kickon.api.global.common.enums.DataStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardReplyKickService{
    private final BoardReplyKickRepository boardReplyKickRepository;
    private final JPAQueryFactory queryFactory;

    // region {findByPk} PK 기반 댓글 킥 조회
    /**
     * PK 기반으로 활성 상태의 댓글 킥을 조회합니다.
     *
     * @param pk 댓글 킥의 PK
     * @return BoardReplyKick 엔티티 (없으면 null)
     */
    public BoardReplyKick findByPk(Long pk) {
        BooleanExpression predicate = QBoardReplyKick.boardReplyKick.pk.eq(pk).and(QBoardReplyKick.boardReplyKick.status.eq(DataStatus.ACTIVATED));
        Optional<BoardReplyKick> boardReplyKickEntity = boardReplyKickRepository.findOne(predicate);
        return boardReplyKickEntity.orElse(null);
    }
    // endregion

    // region {findByBoardReplyAndUser} 댓글 + 사용자 기반 조회
    /**
     * 댓글과 사용자 PK로 해당 사용자의 댓글 킥 여부를 조회합니다.
     *
     * @param boardReplyPk 댓글 PK
     * @param userPk       사용자 PK
     * @return BoardReplyKick 엔티티 (없으면 null)
     */
    public BoardReplyKick findByBoardReplyAndUser(Long boardReplyPk, Long userPk){
        BooleanExpression predicate = QBoardReplyKick.boardReplyKick.boardReply.pk.eq(boardReplyPk).and(QBoardReplyKick.boardReplyKick.user.pk.eq(userPk));
        Optional<BoardReplyKick> boardReplyKickEntity = boardReplyKickRepository.findOne(predicate);
        return boardReplyKickEntity.orElse(null);
    }
    // endregion

    // region {getBoardReplyKickCount} 댓글 킥 개수 조회
    /**
     * 해당 댓글에 대한 전체 킥 수를 조회합니다.
     * 활성 사용자와 활성 킥만 집계합니다.
     *
     * @param boardReplyPk 댓글 PK
     * @return 킥 수
     */
    public Long getBoardReplyKickCount(Long boardReplyPk) {
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
    // endregion

    // region {save} 댓글 킥 저장
    /**
     * 댓글 킥을 저장합니다.
     *
     * @param boardReplyKick 저장할 댓글 킥
     */
    public void save(BoardReplyKick boardReplyKick) {
        boardReplyKickRepository.save(boardReplyKick);
    }
    // endregion
}
