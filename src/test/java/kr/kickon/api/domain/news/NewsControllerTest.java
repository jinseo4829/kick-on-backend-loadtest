package kr.kickon.api.domain.news;

import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.news.dto.*;
import kr.kickon.api.domain.news.request.CreateNewsRequest;
import kr.kickon.api.domain.news.request.GetNewsRequest;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.NewsCategory;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.ForbiddenException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsControllerTest {

    @InjectMocks
    private NewsController newsController; // 실제 Bean

    @Mock
    private NewsService newsService; // Mock

    @Mock
    private UserFavoriteTeamService userFavoriteTeamService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TeamService teamService;

    @Mock
    private LeagueService leagueService;

    // region {getHomeNews}
    @Test
    void testGetHomeNews_NoUser() {
        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(null);
        when(newsService.getRecent3NewsList()).thenReturn(List.of(new NewsListDTO()));

        ResponseEntity<ResponseDTO<List<NewsListDTO>>> response = newsController.getHomeNews(null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ResponseCode.SUCCESS.getCode(), response.getBody().getCode());
        assertFalse(response.getBody().getData().isEmpty());
    }
    // endregion

    // region {getHotNews}
    @Test
    void testGetHotNews() {
        HotNewsListDTO dto = HotNewsListDTO.builder()
                .pk(1L)
                .title("Test News")
                .category("이적")
                .views(123)
                .build();

        when(newsService.getTop5HotNewsList()).thenReturn(List.of(dto));

        ResponseEntity<ResponseDTO<List<HotNewsListDTO>>> response = newsController.getHotNews();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ResponseCode.SUCCESS.getCode(), response.getBody().getCode());
        assertEquals(1, response.getBody().getData().size());
    }

    // endregion

    // region {createNews}
    @Test
    void testCreateNews() {
        User mockUser = new User();
        mockUser.setPk(1L);
        mockUser.setId("user1");

        CreateNewsRequest request = new CreateNewsRequest();
        request.setTitle("title");
        request.setContents("contents");
        request.setCategory(NewsCategory.ETC);

        News mockNews = new News();
        mockNews.setPk(1L);
        mockNews.setUser(mockUser);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(mockUser);
        when(newsService.createNewsWithMedia(any(), any(), any(), any())).thenReturn(mockNews);
        when(newsService.getNewsDetailDTOByPk(any(), any())).thenReturn(new NewsDetailDTO());

        ResponseEntity<ResponseDTO<NewsDetailDTO>> response = newsController.createNews(request);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody().getData());
    }
    // endregion

    // region {getNews}
    @Test
    void testGetNews() {
        User user = new User();
        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);

        GetNewsRequest query = new GetNewsRequest();
        query.setPage(1);
        query.setSize(10);
        query.setOrder("recent");

        PaginatedNewsListDTO mockList = new PaginatedNewsListDTO(1, 10, 1L, List.of(new NewsListDTO()));

        when(newsService.getNewsListWithPagination(any(), anyInt(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(mockList);

        ResponseEntity<ResponseDTO<List<NewsListDTO>>> response = newsController.getNews(query);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody().getMeta());
    }
    // endregion

    // region {getBoardDetail}
    @Test
    void testGetBoardDetail_Found() {
        User user = new User();
        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsService.getNewsDetailDTOByPk(any(), any())).thenReturn(new NewsDetailDTO());

        ResponseEntity<ResponseDTO<NewsDetailDTO>> response = newsController.getBoardDetail(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void testGetBoardDetail_NotFound() {
        User user = new User();
        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsService.getNewsDetailDTOByPk(any(), any())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> newsController.getBoardDetail(1L));
    }
    // endregion

    // region {deleteBoard}
    @Test
    void testDeleteBoard_Success() {
        User user = new User();
        user.setId("user1");

        News news = new News();
        news.setPk(1L);
        news.setUser(user);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsService.findByPk(1L)).thenReturn(news);

        ResponseEntity<ResponseDTO> response = newsController.deleteBoard(1L);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteBoard_NotOwner() {
        User user = new User();
        user.setId("user1");

        User another = new User();
        another.setId("user2");

        News news = new News();
        news.setPk(1L);
        news.setUser(another);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsService.findByPk(1L)).thenReturn(news);

        assertThrows(ForbiddenException.class, () -> newsController.deleteBoard(1L));
    }
    // endregion

    // region {patchNews}
    @Test
    void testPatchNews_Success() {
        User user = new User();
        user.setId("user1");

        News news = new News();
        news.setPk(1L);
        news.setUser(user);

        CreateNewsRequest request = new CreateNewsRequest();
        request.setTitle("new title");
        request.setContents("new contents");
        request.setCategory(NewsCategory.ETC);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsService.findByPk(1L)).thenReturn(news);
        when(newsService.getNewsDetailDTOByPk(any(), any())).thenReturn(new NewsDetailDTO());

        ResponseEntity<ResponseDTO<NewsDetailDTO>> response = newsController.patchNews(1L, request);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void testPatchNews_NotOwner() {
        User user = new User();
        user.setId("user1");

        User another = new User();
        another.setId("user2");

        News news = new News();
        news.setPk(1L);
        news.setUser(another);

        CreateNewsRequest request = new CreateNewsRequest();
        request.setTitle("new title");
        request.setContents("new contents");
        request.setCategory(NewsCategory.ETC);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsService.findByPk(1L)).thenReturn(news);

        assertThrows(ForbiddenException.class, () -> newsController.patchNews(1L, request));
    }
    // endregion
}
