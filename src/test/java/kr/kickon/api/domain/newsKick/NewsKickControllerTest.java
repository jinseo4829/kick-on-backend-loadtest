package kr.kickon.api.domain.newsKick;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.newsKick.request.CreateNewsKickRequest;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.entities.NewsKick;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NewsKickControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private NewsService newsService;

    @Mock
    private NewsKickService newsKickService;

    @InjectMocks
    private NewsKickController newsKickController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(newsKickController).build();
    }

    @Test
    void createNewsKick_firstTime_shouldSave() throws Exception {
        User mockUser = new User();
        mockUser.setPk(1L);
        mockUser.setId("testUser");

        News mockNews = new News();
        mockNews.setPk(10L);

        CreateNewsKickRequest request = new CreateNewsKickRequest();
        request.setNews(10L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
        when(newsService.findByPk(10L)).thenReturn(mockNews);
        when(newsKickService.findByNewsAndUser(10L, 1L)).thenReturn(null);

        mockMvc.perform(post("/api/news-kick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        verify(newsKickService, times(1)).save(any(NewsKick.class));
    }

    @Test
    void createNewsKick_alreadyExists_shouldToggleStatus() throws Exception {
        User mockUser = new User();
        mockUser.setPk(1L);

        News mockNews = new News();
        mockNews.setPk(10L);

        NewsKick mockKick = new NewsKick();
        mockKick.setStatus(DataStatus.ACTIVATED);

        CreateNewsKickRequest request = new CreateNewsKickRequest();
        request.setNews(10L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
        when(newsService.findByPk(10L)).thenReturn(mockNews);
        when(newsKickService.findByNewsAndUser(10L, 1L)).thenReturn(mockKick);

        mockMvc.perform(post("/api/news-kick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.getCode()));

        verify(newsKickService, times(1)).save(mockKick);
        assertThat(mockKick.getStatus()).isEqualTo(DataStatus.DEACTIVATED);
    }

}
