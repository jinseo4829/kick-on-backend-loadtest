package kr.kickon.api.domain.reportNews;

import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.reportNews.request.CreateReportNewsRequestDTO;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportNewsControllerTest {

    @InjectMocks
    private ReportNewsController reportNewsController;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private NewsService newsService;

    @Mock
    private ReportNewsService reportNewsService;

    private User user;
    private News news;

    @BeforeEach
    void setUp() {
        user = new User();
        news = new News();
        news.setPk(1L);
    }

    @Test
    @DisplayName("뉴스 신고 생성 - 성공")
    void createReportNews_success() {
        // given
        CreateReportNewsRequestDTO request = new CreateReportNewsRequestDTO();
        request.setNews(1L);
        request.setReason("허위 정보");

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsService.findByPk(1L)).thenReturn(news);

        // when
        ResponseEntity<ResponseDTO<Void>> response =
                reportNewsController.createReportNews(request);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("GET_SUCCESS");

        verify(reportNewsService, times(1)).save(any(ReportNews.class));
    }

    @Test
    @DisplayName("뉴스 신고 생성 - 실패 (뉴스 없음)")
    void createReportNews_fail_newsNotFound() {
        // given
        CreateReportNewsRequestDTO request = new CreateReportNewsRequestDTO();
        request.setNews(999L);
        request.setReason("스팸");

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsService.findByPk(999L)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> reportNewsController.createReportNews(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ResponseCode.NOT_FOUND_BOARD.getMessage());

        verify(reportNewsService, never()).save(any());
    }
}
