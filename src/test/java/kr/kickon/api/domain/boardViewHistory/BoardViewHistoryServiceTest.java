package kr.kickon.api.domain.boardViewHistory;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.kickon.api.global.common.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardViewHistoryServiceTest {


    @InjectMocks
    private BoardViewHistoryService service;

    @Mock
    private BoardViewHistoryRepository repository;

    @Test
    @DisplayName("BoardViewHistory 조회 성공")
    void findByPk_success() {
        BoardViewHistory mockHistory = BoardViewHistory.builder().pk(1L).build();
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(mockHistory));

        BoardViewHistory result = service.findByPk(1L);

        assertNotNull(result);
        assertEquals(1L, result.getPk());
        verify(repository).findOne(any(BooleanExpression.class));
    }

    @Test
    @DisplayName("findByPk - 존재하지 않으면 null 반환")
    void findByPk_notFound() {
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.empty());

        BoardViewHistory result = service.findByPk(999L);

        assertNull(result);
        verify(repository).findOne(any(BooleanExpression.class));
    }

    @Test
    @DisplayName("BoardViewHistory 저장 성공")
    void save_success() {
        BoardViewHistory history = BoardViewHistory.builder().pk(1L).build();
        when(repository.save(any(BoardViewHistory.class))).thenReturn(history);

        service.save(history);

        verify(repository).save(history);
    }
}