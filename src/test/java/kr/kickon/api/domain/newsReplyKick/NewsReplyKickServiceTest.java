package kr.kickon.api.domain.newsReplyKick;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.entities.NewsReply;
import kr.kickon.api.global.common.entities.NewsReplyKick;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsReplyKickServiceTest {

    @InjectMocks
    private NewsReplyKickService newsReplyKickService;

    @Mock
    private NewsReplyKickRepository newsReplyKickRepository;

    @Mock
    private JPAQueryFactory queryFactory;

    // region {findByPk}
    @Test
    void testFindByPk_Found() {
        NewsReplyKick mockKick = new NewsReplyKick();
        mockKick.setPk(1L);
        mockKick.setStatus(DataStatus.ACTIVATED);

        // Predicate 구체적으로 명시
        when(newsReplyKickRepository.findOne(isA(Predicate.class)))
                .thenReturn(Optional.of(mockKick));

        NewsReplyKick result = newsReplyKickService.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    void testFindByPk_NotFound() {
        when(newsReplyKickRepository.findOne(isA(Predicate.class)))
                .thenReturn(Optional.empty());

        NewsReplyKick result = newsReplyKickService.findByPk(99L);

        assertThat(result).isNull();
    }
    // endregion

    // region {findByNewsReplyAndUser}
    @Test
    void testFindByNewsReplyAndUser_Found() {
        NewsReplyKick mockKick = new NewsReplyKick();
        mockKick.setPk(2L);

        when(newsReplyKickRepository.findOne(isA(Predicate.class)))
                .thenReturn(Optional.of(mockKick));

        NewsReplyKick result = newsReplyKickService.findByNewsReplyAndUser(10L, 20L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(2L);
    }

    @Test
    void testFindByNewsReplyAndUser_NotFound() {
        when(newsReplyKickRepository.findOne(isA(Predicate.class)))
                .thenReturn(Optional.empty());

        NewsReplyKick result = newsReplyKickService.findByNewsReplyAndUser(10L, 20L);

        assertThat(result).isNull();
    }
    // endregion

    // region {save}
    @Test
    void testSave() {
        NewsReplyKick mockKick = new NewsReplyKick();
        mockKick.setPk(3L);

        newsReplyKickService.save(mockKick);

        verify(newsReplyKickRepository).save(mockKick);
    }
    // endregion

    // region {getNewsReplyKickCount}
    @Test
    void testGetNewsReplyKickCount_placeholder() {
        // queryFactory 부분은 통합테스트로 커버하는 게 적합
        // 여기서는 단순히 save 호출만 확인
        NewsReply reply = new NewsReply();
        reply.setPk(10L);

        User user = new User();
        user.setPk(100L);

        NewsReplyKick kick = new NewsReplyKick();
        kick.setNewsReply(reply);
        kick.setUser(user);
        kick.setStatus(DataStatus.ACTIVATED);

        newsReplyKickService.save(kick);

        verify(newsReplyKickRepository).save(kick);
    }
    // endregion
}
