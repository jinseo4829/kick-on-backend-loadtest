package kr.kickon.api.domain.boardReplyKick;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardReplyKickServiceTest {

    @InjectMocks
    private BoardReplyKickService service;

    @Mock
    private BoardReplyKickRepository repository;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private JPAQuery<Long> queryLong;

    // region findByPk 테스트
    @Test
    @DisplayName("findByPk 조회 성공")
    void findByPk_success() {
        BoardReplyKick kick = BoardReplyKick.builder().pk(1L).build();
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(kick));

        BoardReplyKick result = service.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
        verify(repository).findOne(any(BooleanExpression.class));
    }

    @Test
    @DisplayName("findByPk 실패 - 존재하지 않는 PK 조회")
    void findByPk_notFound() {
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.empty());

        BoardReplyKick result = service.findByPk(999L);

        assertThat(result).isNull();
        verify(repository).findOne(any(BooleanExpression.class));
    }
    // endregion

    // region findByBoardReplyAndUser 테스트
    @Test
    @DisplayName("findByBoardReplyAndUser 조회 성공")
    void findByBoardReplyAndUser_success() {
        BoardReplyKick kick = BoardReplyKick.builder().pk(1L).build();
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(kick));

        BoardReplyKick result = service.findByBoardReplyAndUser(10L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
        verify(repository).findOne(any(BooleanExpression.class));
    }

    @Test
    @DisplayName("findByBoardReplyAndUser 실패 - 존재하지 않는 킥 조회")
    void findByBoardReplyAndUser_notFound() {
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.empty());

        BoardReplyKick result = service.findByBoardReplyAndUser(10L, 1L);

        assertThat(result).isNull();
        verify(repository).findOne(any(BooleanExpression.class));
    }
    // endregion

    // region getBoardReplyKickCount 테스트
    @Test
    @DisplayName("getBoardReplyKickCount 성공")
    void getBoardReplyKickCount_success() {
        when(queryFactory.select(any(Expression.class))).thenReturn(queryLong);
        when(queryLong.from((EntityPath<?>) any())).thenReturn(queryLong);
        when(queryLong.where(any(Predicate.class))).thenReturn(queryLong);
        when(queryLong.fetchOne()).thenReturn(5L);

        Long count = service.getBoardReplyKickCount(10L);

        assertThat(count).isEqualTo(5L);
        verify(queryFactory).select(any(Expression.class));
    }

    @Test
    @DisplayName("getBoardReplyKickCount - 조회 결과 없음")
    void getBoardReplyKickCount_zero() {
        when(queryFactory.select(any(Expression.class))).thenReturn(queryLong);
        when(queryLong.from((EntityPath<?>) any())).thenReturn(queryLong);
        when(queryLong.where(any(Predicate.class))).thenReturn(queryLong);
        when(queryLong.fetchOne()).thenReturn(0L);

        Long count = service.getBoardReplyKickCount(10L);

        assertThat(count).isEqualTo(0L);
    }
    // endregion

    // region save 테스트
    @Test
    @DisplayName("save - 댓글 킥 저장")
    void save_success() {
        BoardReplyKick kick = BoardReplyKick.builder().pk(1L).build();
        service.save(kick);
        verify(repository).save(kick);
    }
    // endregion
}