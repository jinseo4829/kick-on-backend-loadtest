package kr.kickon.api.domain.eventBoard;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kr.kickon.api.admin.eventBoard.request.CreateEventBoardRequest;
import kr.kickon.api.admin.eventBoard.request.UpdateBannerOrderRequest;
import kr.kickon.api.admin.eventBoard.request.UpdateEventBoardRequest;
import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.eventBoard.dto.GetEventBoardDTO;
import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.entities.EventBoard;
import kr.kickon.api.global.common.entities.QEventBoard;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.common.enums.UsedInType;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventBoardService {
    private final EventBoardRepository eventBoardRepository;
    private final AwsFileReferenceService awsFileReferenceService;
    private final JPAQueryFactory queryFactory;
    private final AwsService awsService;

    public EventBoard findByPk(Long pk) {
        BooleanExpression predicate = QEventBoard.eventBoard.pk.eq(pk).and(QEventBoard.eventBoard.status.eq(DataStatus.ACTIVATED));
        Optional<EventBoard> eventBoard = eventBoardRepository.findOne(predicate);
        if(eventBoard.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_EVENT_BOARD);
        return eventBoard.get();
    }

    public List<GetEventBoardDTO> findAll() {
        return queryFactory
                .select(Projections.fields(
                        GetEventBoardDTO.class, // DTO 클래스를 명확히 지정
                        QEventBoard.eventBoard.pk,
                        QEventBoard.eventBoard.title,
                        QEventBoard.eventBoard.thumbnailUrl,
                        QEventBoard.eventBoard.embeddedUrl, // 필드명 매칭
                        QEventBoard.eventBoard.orderNum
                ))
                .from(QEventBoard.eventBoard)
                .where(QEventBoard.eventBoard.status.eq(DataStatus.ACTIVATED), QEventBoard.eventBoard.isDisplayed.isTrue())
                .orderBy(QEventBoard.eventBoard.orderNum.asc())
                .fetch();
    }

    @Transactional
    public void updateOrder(List<UpdateBannerOrderRequest.BannerOrder> orders) {
        for (UpdateBannerOrderRequest.BannerOrder order : orders) {
            EventBoard banner = findByPk(order.getPk());
            banner.setOrderNum(order.getOrderNum());
            eventBoardRepository.save(banner);
        }
    }

    public void softDelete(Long pk) {
        EventBoard banner = findByPk(pk);
        banner.softDelete();
        eventBoardRepository.save(banner);
    }

    @Transactional
    public void update(Long pk, UpdateEventBoardRequest request) {
        EventBoard banner = findByPk(pk);

        if (request.getTitle() != null) banner.setTitle(request.getTitle());
        if (request.getThumbnailUrl() != null){
            banner.setThumbnailUrl(request.getThumbnailUrl());

            // 1. "amazonaws.com/" 기준으로 key 추출
            String[] parts = request.getThumbnailUrl().split("amazonaws.com/");
            if (parts.length < 2) {
                throw new BadRequestException(ResponseCode.INVALID_REQUEST);
            }

            String encodedKey = parts[1];
            String decodedKey = URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);

            // 2. 기존 이미지 삭제 처리
            List<AwsFileReference> awsFileReferences = awsFileReferenceService.findByEventBoardPk(pk);

            // 기존 이벤트 배너가 있는 경우 삭제
            for(AwsFileReference awsFileReference : awsFileReferences){
                try (S3Client s3 = S3Client.builder().build()) {
                    awsService.deleteFileFromS3AndDb(s3, awsFileReference); // key가 저장된 컬럼명에 따라 다름
                }
            }

            AwsFileReference awsFileReference = awsFileReferenceService.findByKey(decodedKey);
            if(awsFileReference==null) throw new NotFoundException(ResponseCode.NOT_FOUND_AWS_FILE);
            // AwsFileReference 생성
            awsFileReference.setReferencePk(pk);
            awsFileReference.setUsedIn(UsedInType.EVENT_BOARD);

            awsFileReferenceService.save(awsFileReference);
        }
        if (request.getEmbeddedUrl() != null) banner.setEmbeddedUrl(request.getEmbeddedUrl());
        if (request.getOrderNum() != null) banner.setOrderNum(request.getOrderNum());
        if (request.getIsDisplayed() != null) {
            banner.setIsDisplayed(request.getIsDisplayed());
            if(!banner.getIsDisplayed()) {
                banner.setOrderNum(null);
            }
        }

        eventBoardRepository.save(banner);
    }

    public Page<EventBoard> list(Boolean isDisplayed, Pageable pageable) {
        if (isDisplayed == null) {
            return eventBoardRepository.findAllByStatus(DataStatus.ACTIVATED, pageable);
        }

        if (isDisplayed) {
            return eventBoardRepository.findByIsDisplayedAndStatusOrderByOrderNumAsc(true, DataStatus.ACTIVATED, pageable);
        } else {
            return eventBoardRepository.findByIsDisplayedAndStatusOrderByCreatedAtDesc(false, DataStatus.ACTIVATED, pageable);
        }
    }

    @Transactional
    public void create(CreateEventBoardRequest request) {
        EventBoard banner = new EventBoard();
        banner.setTitle(request.getTitle());
        banner.setThumbnailUrl(request.getThumbnailUrl());
        banner.setEmbeddedUrl(request.getEmbeddedUrl());
        banner.setIsDisplayed(false);

        EventBoard saved = eventBoardRepository.save(banner);
        // 1. "amazonaws.com/" 기준으로 key 추출
        String[] parts = request.getThumbnailUrl().split("amazonaws.com/");
        if (parts.length < 2) {
            throw new BadRequestException(ResponseCode.INVALID_REQUEST);
        }

        String encodedKey = parts[1];
        String decodedKey = URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);
        AwsFileReference awsFileReference = awsFileReferenceService.findByKey(decodedKey);
        if(awsFileReference==null) throw new NotFoundException(ResponseCode.NOT_FOUND_AWS_FILE);
        // AwsFileReference 생성
        awsFileReference.setReferencePk(saved.getPk());
        awsFileReference.setUsedIn(UsedInType.EVENT_BOARD);

        awsFileReferenceService.save(awsFileReference);
    }
}
