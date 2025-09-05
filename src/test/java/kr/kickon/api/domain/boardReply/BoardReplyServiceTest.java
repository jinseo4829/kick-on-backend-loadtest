package kr.kickon.api.domain.boardReply;

import kr.kickon.api.domain.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.querydsl.core.types.dsl.BooleanExpression;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.UsedInType;
import org.junit.jupiter.api.DisplayName;
import org.mockito.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardReplyServiceTest {

    @InjectMocks
    private BoardReplyService boardReplyService;

    @Mock
    private BoardReplyRepository boardReplyRepository;
    @Mock
    private AwsFileReferenceService awsFileReferenceService;
    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("findByPk - 댓글 없음 시 null 반환")
    void findByPk_notFound() {
        when(boardReplyRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.empty());

        BoardReply result = boardReplyService.findByPk(1L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("createBoardReplyWithImages - 이미지 키 전달 시 파일 참조 업데이트 호출")
    void createBoardReplyWithImages_withImages() {
        BoardReply reply = BoardReply.builder().pk(10L).build();
        when(boardReplyRepository.save(any())).thenReturn(reply);

        String[] usedImageKeys = {"img1.png", "img2.png"};

        boardReplyService.createBoardReplyWithImages(reply, usedImageKeys);

        verify(awsFileReferenceService).updateFilesAsUsed(
                argThat(list -> list.size() == 2 && list.get(0).contains("img1.png")),
                eq(UsedInType.BOARD_REPLY),
                eq(10L)
        );
    }

    @Test
    @DisplayName("updateBoardReply - 저장 확인")
    void updateBoardReply_save() {
        BoardReply reply = BoardReply.builder().pk(30L).contents("내용").build();
        when(boardReplyRepository.save(any())).thenReturn(reply);

        BoardReply result = boardReplyService.updateBoardReply(reply);

        verify(boardReplyRepository).save(reply);
        assertThat(result.getContents()).isEqualTo("내용");
    }

    @Test
    @DisplayName("countRepliesByBoardPk - repository 값 반환")
    void countRepliesByBoardPk() {
        Board board = Board.builder().pk(50L).build();
        when(boardReplyRepository.countByBoard_PkAndStatus(50L, DataStatus.ACTIVATED)).thenReturn(5L);

        Long result = boardReplyService.countRepliesByBoardPk(board);

        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("sendReplyNotification - 일반 댓글 알림 발송")
    void sendReplyNotification_boardReply() {
        User boardOwner = User.builder().pk(1L).id("owner").build();
        Board board = Board.builder().pk(100L).user(boardOwner).build();

        User writer = User.builder().pk(2L).id("writer").nickname("작성자").build();

        boardReplyService.sendReplyNotification(board, null, writer);

        verify(notificationService).sendNotification(
                eq(boardOwner),
                eq("BOARD_REPLY"),
                contains("작성자"),
                eq("/board/100")
        );
    }

    @Test
    @DisplayName("sendReplyNotification - 대댓글 알림 발송")
    void sendReplyNotification_childReply() {
        User parentUser = User.builder().pk(3L).id("parent").build();
        BoardReply parentReply = BoardReply.builder().pk(200L).user(parentUser).build();

        Board board = Board.builder().pk(100L).user(User.builder().pk(99L).id("boardUser").build()).build();
        User writer = User.builder().pk(4L).id("writer").nickname("작성자").build();

        boardReplyService.sendReplyNotification(board, parentReply, writer);

        verify(notificationService).sendNotification(
                eq(parentUser),
                eq("BOARD_REPLY_REPLY"),
                contains("작성자"),
                eq("/board/100?replyPk=200")
        );
    }

    @Test
    @DisplayName("sendReplyNotification - 본인 댓글인 경우 알림 없음")
    void sendReplyNotification_self() {
        User boardOwner = User.builder().pk(1L).id("sameUser").nickname("본인").build();
        Board board = Board.builder().pk(100L).user(boardOwner).build();

        // 본인이 자기 게시글에 댓글 단 경우
        boardReplyService.sendReplyNotification(board, null, boardOwner);

        verify(notificationService, never()).sendNotification(any(), any(), any(), any());
    }
}