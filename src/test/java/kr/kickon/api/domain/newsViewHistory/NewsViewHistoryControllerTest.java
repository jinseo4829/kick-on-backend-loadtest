package kr.kickon.api.domain.newsViewHistory;

import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.newsViewHistory.request.CreateNewsViewHistoryRequest;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.entities.NewsViewHistory;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NewsViewHistoryControllerTest {

    @InjectMocks
    private NewsViewHistoryController newsViewHistoryController;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private NewsViewHistoryService newsViewHistoryService;

    @Mock
    private NewsService newsService;

    // region {createNewsView}
    @Test
    void testCreateNewsView_withUser() {
        // given
        User user = new User(); user.setPk(1L);
        News news = new News(); news.setPk(10L);

        CreateNewsViewHistoryRequest body = new CreateNewsViewHistoryRequest();
        body.setNews(10L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsService.findByPk(10L)).thenReturn(news);

        // when
        ResponseEntity<ResponseDTO<Void>> response = newsViewHistoryController.createNewsView(body);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(ResponseCode.SUCCESS.getCode());

        verify(newsViewHistoryService).save(isA(NewsViewHistory.class));
    }

    @Test
    void testCreateNewsView_withoutUser() {
        // given
        News news = new News(); news.setPk(20L);

        CreateNewsViewHistoryRequest body = new CreateNewsViewHistoryRequest();
        body.setNews(20L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(null);
        when(newsService.findByPk(20L)).thenReturn(news);

        // when
        ResponseEntity<ResponseDTO<Void>> response = newsViewHistoryController.createNewsView(body);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(ResponseCode.SUCCESS.getCode());

        verify(newsViewHistoryService).save(isA(NewsViewHistory.class));
    }

    @Test
    void testCreateNewsView_newsNotFound() {
        // given
        CreateNewsViewHistoryRequest body = new CreateNewsViewHistoryRequest();
        body.setNews(999L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(new User());
        when(newsService.findByPk(999L)).thenReturn(null);

        // then
        assertThrows(NotFoundException.class, () -> newsViewHistoryController.createNewsView(body));
    }
    // endregion
}
