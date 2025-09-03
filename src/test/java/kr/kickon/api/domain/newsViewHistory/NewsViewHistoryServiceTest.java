package kr.kickon.api.domain.newsViewHistory;

import com.querydsl.core.types.Predicate;
import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.entities.NewsViewHistory;
import kr.kickon.api.global.common.enums.DataStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsViewHistoryServiceTest {

    @Mock
    private NewsViewHistoryRepository newsViewHistoryRepository;

    @InjectMocks
    private NewsViewHistoryService newsViewHistoryService;

    // region {findByPk}
    @Test
    void testFindByPk_Found() {
        // given
        NewsViewHistory history = new NewsViewHistory();
        history.setPk(1L);
        history.setStatus(DataStatus.ACTIVATED);

        when(newsViewHistoryRepository.findOne(isA(Predicate.class)))
                .thenReturn(Optional.of(history));

        // when
        NewsViewHistory result = newsViewHistoryService.findByPk(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    void testFindByPk_NotFound() {
        when(newsViewHistoryRepository.findOne(isA(Predicate.class)))
                .thenReturn(Optional.empty());

        NewsViewHistory result = newsViewHistoryService.findByPk(99L);

        assertThat(result).isNull();
    }
    // endregion

    // region {save}
    @Test
    void testSave() {
        NewsViewHistory history = new NewsViewHistory();
        history.setPk(2L);

        when(newsViewHistoryRepository.save(isA(NewsViewHistory.class)))
                .thenReturn(history);

        newsViewHistoryService.save(history);

        verify(newsViewHistoryRepository, times(1)).save(history);
    }
    // endregion

    // region {countViewsByNewsPk}
    @Test
    void testCountViewsByNewsPk() {
        News news = new News();
        news.setPk(10L);

        when(newsViewHistoryRepository.countByNews_Pk(10L))
                .thenReturn(5L);

        Long count = newsViewHistoryService.countViewsByNewsPk(news);

        assertThat(count).isEqualTo(5L);
    }
    // endregion
}
