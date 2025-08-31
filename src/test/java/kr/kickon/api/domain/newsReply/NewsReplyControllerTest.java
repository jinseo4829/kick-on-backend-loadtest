package kr.kickon.api.domain.newsReply;

import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.newsReply.dto.PaginatedNewsReplyListDTO;
import kr.kickon.api.domain.newsReply.dto.ReplyDTO;
import kr.kickon.api.domain.newsReply.request.CreateNewsReplyRequest;
import kr.kickon.api.domain.newsReply.request.GetNewsRepliesRequest;
import kr.kickon.api.domain.newsReply.request.PatchNewsReplyRequest;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.ForbiddenException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsReplyControllerTest {

    @InjectMocks
    private NewsReplyController newsReplyController;

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private NewsReplyService newsReplyService;
    @Mock private UserFavoriteTeamService userFavoriteTeamService;
    @Mock private NewsService newsService;
    @Mock private UserService userService;

    // region {createNewsReply}
    @Test
    void testCreateNewsReply_Success() {
        Team team = new Team(); team.setPk(1L);
        User user = new User(); user.setPk(1L); user.setId("u1");
        News news = new News(); news.setPk(10L); news.setTeam(team);

        CreateNewsReplyRequest request = new CreateNewsReplyRequest();
        request.setNews(10L);
        request.setContents("댓글내용");

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(userService.findByPk(1L)).thenReturn(user);
        when(newsService.findByPk(10L)).thenReturn(news);
        when(userFavoriteTeamService.findAllByUserPk(1L))
                .thenReturn(List.of(UserFavoriteTeam.builder().user(user).team(news.getTeam()).build()));
        when(newsReplyService.createNewsReplyWithImages(any(), any())).thenReturn(new NewsReply());

        ResponseEntity<ResponseDTO<Void>> response = newsReplyController.createNewsReply(request);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(ResponseCode.SUCCESS.getCode());
    }

    @Test
    void testCreateNewsReply_NewsNotFound() {
        User user = new User(); user.setPk(1L);
        CreateNewsReplyRequest request = new CreateNewsReplyRequest();
        request.setNews(99L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(userService.findByPk(1L)).thenReturn(user);
        when(newsService.findByPk(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> newsReplyController.createNewsReply(request));
    }

    @Test
    void testCreateNewsReply_Forbidden_NoTeamMatch() {
        Team team = new Team(); team.setPk(2L);
        User user = new User(); user.setPk(1L);
        News news = new News(); news.setPk(10L); news.setTeam(team);

        CreateNewsReplyRequest request = new CreateNewsReplyRequest();
        request.setNews(10L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(userService.findByPk(1L)).thenReturn(user);
        when(newsService.findByPk(10L)).thenReturn(news);
        when(userFavoriteTeamService.findAllByUserPk(1L)).thenReturn(List.of()); // 팀 없음

        assertThrows(ForbiddenException.class, () -> newsReplyController.createNewsReply(request));
    }
    // endregion

    @Test
    void testGetNewsReplies_NotFoundNews() {
        GetNewsRepliesRequest query = new GetNewsRepliesRequest();
        query.setNews(99L);

        when(newsService.findByPk(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> newsReplyController.getNewsReplies(query));
    }
    // endregion

    // region {deleteNewsReplies}
    @Test
    void testDeleteNewsReply_Success() {
        User user = new User(); user.setId("u1");
        NewsReply reply = new NewsReply(); reply.setUser(user);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsReplyService.findByPk(1L)).thenReturn(reply);

        ResponseEntity<ResponseDTO> response = newsReplyController.deleteNewsReplies(1L);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteNewsReply_Forbidden() {
        User user = new User(); user.setId("u1");
        User other = new User(); other.setId("u2");

        NewsReply reply = new NewsReply(); reply.setUser(other);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsReplyService.findByPk(1L)).thenReturn(reply);

        assertThrows(ForbiddenException.class, () -> newsReplyController.deleteNewsReplies(1L));
    }
    // endregion

    // region {patchNewsReply}
    @Test
    void testPatchNewsReply_Success() {
        User user = new User(); user.setId("u1");
        NewsReply reply = new NewsReply(); reply.setUser(user);

        PatchNewsReplyRequest req = new PatchNewsReplyRequest();
        req.setContents("new content");

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsReplyService.findByPk(1L)).thenReturn(reply);

        ResponseEntity<ResponseDTO<Void>> response = newsReplyController.patchNewsReply(1L, req);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testPatchNewsReply_Forbidden() {
        User user = new User(); user.setId("u1");
        User other = new User(); other.setId("u2");
        NewsReply reply = new NewsReply(); reply.setUser(other);

        PatchNewsReplyRequest req = new PatchNewsReplyRequest();
        req.setContents("new content");

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsReplyService.findByPk(1L)).thenReturn(reply);

        assertThrows(ForbiddenException.class, () -> newsReplyController.patchNewsReply(1L, req));
    }
    // endregion
}
