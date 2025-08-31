package kr.kickon.api.domain.newsReplyKick;

import kr.kickon.api.domain.newsReply.NewsReplyService;
import kr.kickon.api.domain.newsReplyKick.request.CreateNewsReplyKickRequest;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.NewsReply;
import kr.kickon.api.global.common.entities.NewsReplyKick;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsReplyKickControllerTest {

    @InjectMocks
    private NewsReplyKickController controller;

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private NewsReplyService newsReplyService;
    @Mock
    private NewsReplyKickService newsReplyKickService;

    @Test
    void testCreateNewsReplyKick_NewKick() {
        // given
        User user = new User(); user.setPk(1L);
        NewsReply reply = new NewsReply(); reply.setPk(10L);
        CreateNewsReplyKickRequest req = new CreateNewsReplyKickRequest();
        req.setReply(10L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsReplyService.findByPk(10L)).thenReturn(reply);
        when(newsReplyKickService.findByNewsReplyAndUser(10L, 1L)).thenReturn(null);
        doNothing().when(newsReplyKickService).save(any(NewsReplyKick.class));

        // when
        ResponseEntity<ResponseDTO<Void>> response = controller.createNewsReplyKick(req);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(ResponseCode.SUCCESS.getCode());
        verify(newsReplyKickService, times(1)).save(any(NewsReplyKick.class));
    }

    @Test
    void testCreateNewsReplyKick_ToggleExistingKick() {
        // given
        User user = new User(); user.setPk(1L);
        NewsReply reply = new NewsReply(); reply.setPk(20L);
        CreateNewsReplyKickRequest req = new CreateNewsReplyKickRequest();
        req.setReply(20L);

        NewsReplyKick existingKick = new NewsReplyKick();
        existingKick.setPk(5L);
        existingKick.setStatus(DataStatus.ACTIVATED);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsReplyService.findByPk(20L)).thenReturn(reply);
        when(newsReplyKickService.findByNewsReplyAndUser(20L, 1L)).thenReturn(existingKick);
        doNothing().when(newsReplyKickService).save(any(NewsReplyKick.class));

        // when
        ResponseEntity<ResponseDTO<Void>> response = controller.createNewsReplyKick(req);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(existingKick.getStatus()).isEqualTo(DataStatus.DEACTIVATED); // 토글 확인
        verify(newsReplyKickService, times(1)).save(existingKick);
    }

    @Test
    void testCreateNewsReplyKick_NotFoundReply() {
        // given
        User user = new User(); user.setPk(1L);
        CreateNewsReplyKickRequest req = new CreateNewsReplyKickRequest();
        req.setReply(99L);

        when(jwtTokenProvider.getUserFromSecurityContext()).thenReturn(user);
        when(newsReplyService.findByPk(99L)).thenReturn(null);

        // when & then
        assertThrows(NotFoundException.class, () -> controller.createNewsReplyKick(req));
    }
}
