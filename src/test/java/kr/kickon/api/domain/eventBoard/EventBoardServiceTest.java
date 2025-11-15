package kr.kickon.api.domain.eventBoard;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.admin.eventBoard.request.CreateEventBoardRequest;
import kr.kickon.api.admin.eventBoard.request.UpdateBannerOrderRequest;
import kr.kickon.api.admin.eventBoard.request.UpdateEventBoardRequest;
import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.eventBoard.dto.GetEventBoardDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventBoardServiceTest {

    @InjectMocks
    private EventBoardService eventBoardService;

    @Mock
    private EventBoardRepository eventBoardRepository;

    @Mock
    private AwsFileReferenceService awsFileReferenceService;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private JPAQuery<GetEventBoardDTO> jpaQuery; // Querydsl mock

    private EventBoard board;

    @BeforeEach
    void setUp() {
        board = new EventBoard();
        board.setPk(1L);
        board.setTitle("Event 1");
        board.setThumbnailUrl("https://s3.amazonaws.com/bucket/logo.png");
        board.setEmbeddedUrl("https://youtube.com/video");
        board.setIsDisplayed(true);
    }

    @Test
    @DisplayName("findByPk - 존재하는 배너 조회")
    void findByPk_success() {
        when(eventBoardRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(board));

        EventBoard result = eventBoardService.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByPk - 없는 배너 조회 시 NotFoundException")
    void findByPk_notFound() {
        when(eventBoardRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventBoardService.findByPk(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ResponseCode.NOT_FOUND_EVENT_BOARD.getMessage());
    }

    @Test
    @DisplayName("findAll - 활성화된 이벤트 배너 조회")
    void findAll_success() {
        GetEventBoardDTO expectedDto = new GetEventBoardDTO();

        expectedDto.setTitle("Event 1"); // DTO에 필드가 있다고 가정
        when(queryFactory.select(any(Expression.class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(QEventBoard.class))).thenReturn(jpaQuery);
        doReturn(jpaQuery).when(jpaQuery).where(any(BooleanExpression.class), any(BooleanExpression.class));

        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetch()).thenReturn(List.of(expectedDto));

        // When
        List<GetEventBoardDTO> result = eventBoardService.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Event 1");

        // Verify
        verify(queryFactory).select(any(Expression.class));
        verify(jpaQuery).from(any(QEventBoard.class));
        verify(jpaQuery).where(any(BooleanExpression.class), any(BooleanExpression.class));
        verify(jpaQuery).orderBy(any(OrderSpecifier.class));
        verify(jpaQuery).fetch();
    }

    @Test
    @DisplayName("softDelete - 배너 삭제 처리")
    void softDelete_success() {
        when(eventBoardRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(board));
        when(eventBoardRepository.save(any(EventBoard.class))).thenReturn(board);

        eventBoardService.softDelete(1L);

        assertThat(board.getStatus()).isEqualTo(DataStatus.DEACTIVATED); // softDelete 구현에 따라
        verify(eventBoardRepository, times(1)).save(board);
    }

    @Test
    @DisplayName("updateOrder - 배너 순서 업데이트")
    void updateOrder_success() {
        when(eventBoardRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(board));
        when(eventBoardRepository.save(any(EventBoard.class))).thenReturn(board);

        UpdateBannerOrderRequest.BannerOrder order = new UpdateBannerOrderRequest.BannerOrder();
        order.setPk(1L);
        order.setOrderNum(5);

        eventBoardService.updateOrder(List.of(order));

        assertThat(board.getOrderNum()).isEqualTo(5);
        verify(eventBoardRepository, times(1)).save(board);
    }

    @Test
    @DisplayName("update - 제목, 썸네일, URL 업데이트")
    void update_success() {
        when(eventBoardRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(board));
        when(eventBoardRepository.save(any(EventBoard.class))).thenReturn(board);
        when(awsFileReferenceService.findByEventBoardPk(anyLong())).thenReturn(List.of());
        when(awsFileReferenceService.findByKey(anyString())).thenReturn(new AwsFileReference());

        UpdateEventBoardRequest request = new UpdateEventBoardRequest();
        request.setTitle("New Title");
        request.setThumbnailUrl("https://s3.amazonaws.com/bucket/new.png");
        request.setEmbeddedUrl("https://youtube.com/newvideo");
        request.setOrderNum(2);
        request.setIsDisplayed(true);

        eventBoardService.update(1L, request);

        assertThat(board.getTitle()).isEqualTo("New Title");
        assertThat(board.getThumbnailUrl()).isEqualTo("https://s3.amazonaws.com/bucket/new.png");
        assertThat(board.getEmbeddedUrl()).isEqualTo("https://youtube.com/newvideo");
        assertThat(board.getOrderNum()).isEqualTo(2);
        assertThat(board.getIsDisplayed()).isTrue();
        verify(eventBoardRepository, times(1)).save(board);
    }

    @Test
    @DisplayName("create - 새 이벤트 배너 생성")
    void create_success() {
        when(eventBoardRepository.save(any(EventBoard.class))).thenReturn(board);
        AwsFileReference awsFileReference = new AwsFileReference();
        when(awsFileReferenceService.findByKey(anyString())).thenReturn(awsFileReference);
        doNothing().when(awsFileReferenceService).save(any(AwsFileReference.class));

        CreateEventBoardRequest request = new CreateEventBoardRequest();
        request.setTitle("New Event");
        request.setThumbnailUrl("https://s3.amazonaws.com/bucket/logo.png");
        request.setEmbeddedUrl("https://youtube.com/video");

        eventBoardService.create(request);

        assertThat(board.getTitle()).isEqualTo("Event 1"); // saved banner의 title은 mock save 반환값
        verify(eventBoardRepository, times(1)).save(any(EventBoard.class));
        verify(awsFileReferenceService, times(1)).save(any(AwsFileReference.class));
    }
}
