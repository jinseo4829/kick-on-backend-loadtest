package kr.kickon.api.domain.newsKick;

import com.querydsl.core.types.Predicate;
import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.entities.NewsKick;
import kr.kickon.api.global.common.enums.DataStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) //
class NewsKickServiceTest {

    @InjectMocks
    private NewsKickService newsKickService; // 실제 테스트 대상

    @Mock
    private NewsKickRepository newsKickRepository; // Mock Repository

    // region {findByPk}
    @Test
    void testFindByPk_Found() {
        NewsKick mockKick = new NewsKick();
        mockKick.setPk(1L);
        mockKick.setStatus(DataStatus.ACTIVATED);

        when(newsKickRepository.findOne(any(Predicate.class)))
                .thenReturn(Optional.of(mockKick));

        NewsKick result = newsKickService.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    void testFindByPk_NotFound() {
        when(newsKickRepository.findOne(any(Predicate.class)))
                .thenReturn(Optional.empty());

        NewsKick result = newsKickService.findByPk(99L);

        assertThat(result).isNull();
    }
    // endregion

    // region {findByNewsAndUser}
    @Test
    void testFindByNewsAndUser_Found() {
        NewsKick mockKick = new NewsKick();
        mockKick.setPk(2L);
        mockKick.setStatus(DataStatus.ACTIVATED);

        when(newsKickRepository.findOne(any(Predicate.class)))
                .thenReturn(Optional.of(mockKick));

        NewsKick result = newsKickService.findByNewsAndUser(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(2L);
    }

    @Test
    void testFindByNewsAndUser_NotFound() {
        when(newsKickRepository.findOne(any(Predicate.class)))
                .thenReturn(Optional.empty());

        NewsKick result = newsKickService.findByNewsAndUser(1L, 1L);

        assertThat(result).isNull();
    }
    // endregion

    // region {save}
    @Test
    void testSave() {
        NewsKick mockKick = new NewsKick();
        mockKick.setPk(3L);

        when(newsKickRepository.save(any(NewsKick.class)))
                .thenReturn(mockKick);

        newsKickService.save(mockKick);

        assertThat(mockKick.getPk()).isEqualTo(3L);
    }
    // endregion

    // region {countKicksByNewsPk}
    @Test
    void testCountKicksByNewsPk() {
        News news = new News();
        news.setPk(1L);

        when(newsKickRepository.countByNews_PkAndStatus(news.getPk(), DataStatus.ACTIVATED))
                .thenReturn(5L);

        Long result = newsKickService.countKicksByNewsPk(news);

        assertThat(result).isEqualTo(5L);
    }
    // endregion
}
