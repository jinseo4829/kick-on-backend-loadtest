package kr.kickon.api.domain.newsReply;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.newsReply.dto.PaginatedNewsReplyListDTO;
import kr.kickon.api.domain.newsReply.dto.ReplyDTO;
import kr.kickon.api.domain.newsReplyKick.NewsReplyKickService;
import kr.kickon.api.domain.notification.NotificationService;
import kr.kickon.api.domain.teamReporter.TeamReporterService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.UsedInType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsReplyServiceTest {

    @Mock private NewsReplyRepository newsReplyRepository;
    @Mock private JPAQueryFactory queryFactory;
    @Mock private NewsReplyKickService newsReplyKickService;
    @Mock private AwsFileReferenceService awsFileReferenceService;
    @Mock private AwsService awsService;
    @Mock private NotificationService notificationService;
    @Mock private TeamReporterService teamReporterService;

    @InjectMocks private NewsReplyService newsReplyService;

    // region {findByPk}
    @Test
    void testFindByPk_Found() {
        NewsReply reply = new NewsReply();
        reply.setPk(1L);
        reply.setStatus(DataStatus.ACTIVATED);

        when(newsReplyRepository.findOne(any(Predicate.class))).thenReturn(Optional.of(reply));

        NewsReply result = newsReplyService.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    void testFindByPk_NotFound() {
        when(newsReplyRepository.findOne(any(Predicate.class))).thenReturn(Optional.empty());

        NewsReply result = newsReplyService.findByPk(99L);

        assertThat(result).isNull();
    }
    // endregion

    // region {createNewsReplyWithImages}
    @Test
    void testCreateNewsReplyWithImages_WithImages() {
        NewsReply reply = new NewsReply(); reply.setPk(10L);

        when(newsReplyRepository.save(any())).thenReturn(reply);

        NewsReply result = newsReplyService.createNewsReplyWithImages(reply, new String[]{"a.png"});

        assertThat(result).isNotNull();
        verify(awsFileReferenceService, times(1))
                .updateFilesAsUsed(anyList(), eq(UsedInType.NEWS_REPLY), eq(10L));
    }
    // endregion

    // region {updateNewsReply}
    @Test
    void testUpdateNewsReply() {
        NewsReply reply = new NewsReply(); reply.setPk(3L);
        when(newsReplyRepository.save(any())).thenReturn(reply);

        NewsReply result = newsReplyService.updateNewsReply(reply);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(3L);
    }
    // endregion

    // region {countRepliesByNewsPk}
    @Test
    void testCountRepliesByNewsPk() {
        News news = new News(); news.setPk(7L);

        when(newsReplyRepository.countByNews_PkAndStatus(7L, DataStatus.ACTIVATED)).thenReturn(15L);

        Long result = newsReplyService.countRepliesByNewsPk(news);

        assertThat(result).isEqualTo(15L);
    }
    // endregion

    // region {sendReplyNotification}
    @Test
    void testSendReplyNotification_ToNewsOwner() {
        User owner = new User(); owner.setPk(1L);
        User writer = new User(); writer.setPk(2L); writer.setNickname("작성자");

        News news = new News(); news.setPk(10L); news.setUser(owner);

        newsReplyService.sendReplyNotification(news, null, writer);

        verify(notificationService, times(1)).sendNotification(
                eq(owner),
                eq("NEWS_REPLY"),
                contains("작성자님이 회원님의 뉴스에 댓글을 달았어요."),
                eq("/news/10")
        );
    }

    @Test
    void testSendReplyNotification_ToParentReplyOwner() {
        User parentUser = new User(); parentUser.setPk(1L);
        User writer = new User(); writer.setPk(2L); writer.setNickname("작성자");

        News news = new News(); news.setPk(20L); news.setUser(new User());

        NewsReply parentReply = new NewsReply(); parentReply.setPk(5L); parentReply.setUser(parentUser);

        newsReplyService.sendReplyNotification(news, parentReply, writer);

        verify(notificationService, times(1)).sendNotification(
                eq(parentUser),
                eq("NEWS_REPLY_REPLY"),
                contains("작성자님이 답글을 달았어요."),
                eq("/news/20?replyPk=5")
        );
    }
    // endregion
}

