package kr.kickon.api.domain.eventBoard;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import kr.kickon.api.admin.eventBoard.request.CreateEventBoardRequest;
import kr.kickon.api.admin.eventBoard.request.UpdateBannerOrderRequest;
import kr.kickon.api.admin.eventBoard.request.UpdateEventBoardRequest;
import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.eventBoard.dto.GetEventBoardDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.entities.EventBoard;
import kr.kickon.api.global.common.entities.QEventBoard;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.common.enums.UsedInType;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventBoardService implements BaseService<EventBoard> {
    private final EventBoardRepository eventBoardRepository;
    private final AwsFileReferenceService awsFileReferenceService;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final AwsService awsService;

    @Override
    public EventBoard findById(String uuid) {
        BooleanExpression predicate = QEventBoard.eventBoard.id.eq(uuid).and(QEventBoard.eventBoard.status.eq(DataStatus.ACTIVATED));
        Optional<EventBoard> eventBoard = eventBoardRepository.findOne(predicate);
        if(eventBoard.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_EVENT_BOARD);
        return eventBoard.get();
    }

    @Override
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
                        QEventBoard.eventBoard.id,
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

            // 기존 프로필 사진이 있는 경우 삭제
            for(AwsFileReference awsFileReference : awsFileReferences){
                try (S3Client s3 = S3Client.builder().build()) {
                    awsService.deleteFileFromS3AndDb(s3, awsFileReference); // key가 저장된 컬럼명에 따라 다름
                }
            }

            // 3. 새로운 프로필 이미지 등록
            AwsFileReference newProfileImage = awsFileReferenceService.findByKey(decodedKey);
            newProfileImage.setUsedIn(UsedInType.EVENT_BOARD);
            newProfileImage.setReferencePk(pk);
            awsFileReferenceService.save(newProfileImage);
        }
        if (request.getEmbeddedUrl() != null) banner.setEmbeddedUrl(request.getEmbeddedUrl());
        if (request.getOrderNum() != null) banner.setOrderNum(request.getOrderNum());
        if (request.getIsDisplayed() != null) banner.setIsDisplayed(request.getIsDisplayed());

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
        banner.setId(UUID.randomUUID().toString());
        banner.setTitle(request.getTitle());
        banner.setThumbnailUrl(request.getThumbnailUrl());
        banner.setEmbeddedUrl(request.getEmbeddedUrl());
        banner.setOrderNum(request.getOrderNum());
        banner.setIsDisplayed(false);

        EventBoard saved = eventBoardRepository.save(banner);
        // 1. "amazonaws.com/" 기준으로 key 추출
        String[] parts = request.getThumbnailUrl().split("amazonaws.com/");
        if (parts.length < 2) {
            throw new BadRequestException(ResponseCode.INVALID_REQUEST);
        }

        String encodedKey = parts[1];
        String decodedKey = URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);
        // AwsFileReference 생성
        AwsFileReference ref = AwsFileReference.builder()
                .s3Key(decodedKey)
                .usedIn(UsedInType.EVENT_BOARD)
                .referencePk(saved.getPk())
                .id(UUID.randomUUID().toString())
                .build();
        awsFileReferenceService.save(ref);
    }
}
