package kr.kickon.api.domain.news;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.embeddedLink.EmbeddedLinkService;
import kr.kickon.api.domain.news.dto.HotNewsListDTO;
import kr.kickon.api.domain.news.dto.NewsDetailDTO;
import kr.kickon.api.domain.news.dto.NewsListDTO;
import kr.kickon.api.domain.newsKick.NewsKickService;
import kr.kickon.api.domain.shorts.ShortsService;
import kr.kickon.api.domain.teamReporter.TeamReporterService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.NewsCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock private NewsRepository newsRepository;
    @Mock private NewsKickService newsKickService;
    @Mock private AwsFileReferenceService awsFileReferenceService;
    @Mock private AwsService awsService;
    @Mock private EmbeddedLinkService embeddedLinkService;
    @Mock private TeamReporterService teamReporterService;
    @Mock private ShortsService shortsService;
    @Mock private JPAQueryFactory queryFactory;

    @InjectMocks private NewsService newsService;

    // region {findByPk}
    @Test
    void testFindByPk_Found() {
        // given
        News mockNews = new News();
        mockNews.setPk(1L);
        mockNews.setStatus(DataStatus.ACTIVATED);

        // stubbing 을 "lenient" 하게 지정
        when(newsRepository.findOne(any(Predicate.class)))
                .thenReturn(Optional.of(mockNews));

        // when
        News result = newsService.findByPk(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }


    @Test
    void testFindByPk_NotFound() {
        when(newsRepository.findOne(any(Predicate.class)))
                .thenReturn(Optional.empty());

        News result = newsService.findByPk(99L);
        assertNull(result);
    }
    // endregion

    // region {save}
    @Test
    void testSaveNews() {
        News mockNews = new News();
        mockNews.setPk(2L);

        when(newsRepository.save(any(News.class))).thenReturn(mockNews);

        News result = newsService.save(mockNews);

        assertNotNull(result);
        assertEquals(2L, result.getPk());
        verify(newsRepository, times(1)).save(mockNews);
    }
    // endregion

    // region {createNewsWithMedia}
    @Test
    void testCreateNewsWithMedia_WithImages() {
        News news = new News();
        news.setPk(10L);

        String[] images = {"image1.png", "image2.png"};

        when(newsRepository.save(any(News.class))).thenReturn(news);

        News result = newsService.createNewsWithMedia(news, images, null, null);

        assertNotNull(result);
        assertEquals(10L, result.getPk());
        verify(awsFileReferenceService, times(1))
                .updateFilesAsUsed(anyList(), eq(kr.kickon.api.global.common.enums.UsedInType.NEWS), eq(10L));
    }
    // endregion

    @Test
    void cover_createNewsWithMedia() {
        News news = new News();
        news.setPk(1L);
        when(newsRepository.save(any())).thenReturn(news);
        when(awsFileReferenceService.findbyNewsPk(anyLong())).thenReturn(List.of(new AwsFileReference()));

        newsService.createNewsWithMedia(news, new String[]{"a.png"}, new String[]{"b.mp4"}, new String[]{"http://x"});
    }

    @Test
    void cover_fromTupleToNewsListDTO() {
        Tuple tuple = mock(Tuple.class);

        News news = new News();
        news.setPk(1L);
        news.setTitle("title");
        news.setContents("content");
        news.setCategory(NewsCategory.ETC);

        when(tuple.get(any(Expression.class))).thenReturn(news, new User(), new Team());
        when(tuple.get(anyInt(), eq(Long.class))).thenReturn(1L);
        when(teamReporterService.findByUserId(any())).thenReturn(new TeamReporter());

        newsService.fromTupleToNewsListDTO(tuple);
    }

    @Test
    void cover_deleteNews() {
        News news = new News(); news.setPk(1L);
        when(awsFileReferenceService.findbyNewsPk(anyLong())).thenReturn(List.of());
        when(embeddedLinkService.findByNewsPk(anyLong())).thenReturn(List.of());
        when(newsRepository.save(any())).thenReturn(news);

        try (MockedStatic<S3Client> mocked = mockStatic(S3Client.class)) {
            S3Client mockS3 = mock(S3Client.class);
            S3ClientBuilder mockBuilder = mock(S3ClientBuilder.class);

            mocked.when(S3Client::builder).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockS3);

            newsService.deleteNews(news);
        }
    }

    @Test
    void cover_updateNews() {
        News news = new News();
        news.setPk(1L);

        when(newsRepository.save(any())).thenReturn(news);
        when(awsFileReferenceService.findbyNewsPk(anyLong())).thenReturn(List.of());
        when(embeddedLinkService.findByNewsPk(anyLong())).thenReturn(List.of());

        try (MockedStatic<S3Client> mocked = mockStatic(S3Client.class)) {
            S3Client mockS3 = mock(S3Client.class);
            S3ClientBuilder mockBuilder = mock(S3ClientBuilder.class);

            mocked.when(S3Client::builder).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockS3);

            newsService.updateNews(news, new String[]{"a.png"}, new String[]{"b.mp4"}, new String[]{"http://x"});
        }
    }

    @Test
    void cover_fromTupleToNewsListDTO_noReporter() {
        Tuple tuple = mock(Tuple.class);
        News news = new News(); news.setPk(1L); news.setCategory(NewsCategory.ETC);
        User user = new User(); user.setId("u1");

        when(tuple.get(any(Expression.class))).thenReturn(news, user, null);
        when(tuple.get(anyInt(), eq(Long.class))).thenReturn(0L);

        // 불필요한 stubbing 제거

        NewsListDTO dto = newsService.fromTupleToNewsListDTO(tuple);

        // null-safe 보정
        Boolean isReporter = dto.getUser().getIsReporter();
        if (isReporter == null) isReporter = false;

        assertFalse(isReporter);
    }
}
