package kr.kickon.api.domain.awsFileReference;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import java.util.stream.Collectors;

import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.common.enums.UsedInType;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AwsFileReferenceService implements BaseService<AwsFileReference> {
    private final AwsFileReferenceRepository awsFileReferenceRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    //    public List<User> findUserByEmail(String email){
//        // JPAQueryFactory
//        return queryFactory.selectFrom(QUser.user)
//                .where(QUser.user.email.eq(email))
//                .fetch();
//    }
    @Override
    public AwsFileReference findById(String uuid) {
        BooleanExpression predicate = QAwsFileReference.awsFileReference.id.eq(uuid).and(QAwsFileReference.awsFileReference.status.eq(DataStatus.ACTIVATED));
        Optional<AwsFileReference> awsFileReference = awsFileReferenceRepository.findOne(predicate);
        return awsFileReference.orElse(null);
    }

    @Override
    public AwsFileReference findByPk(Long pk) {
        BooleanExpression predicate = QAwsFileReference.awsFileReference.pk.eq(pk).and(QAwsFileReference.awsFileReference.status.eq(DataStatus.ACTIVATED));
        Optional<AwsFileReference> awsFileReference = awsFileReferenceRepository.findOne(predicate);
        return awsFileReference.orElse(null);
    }

    @Transactional
    public void updateFilesAsUsed(List<String> s3Keys, UsedInType usedIn, Long referencePk) {
        List<AwsFileReference> files = awsFileReferenceRepository.findAllByS3KeyIn(s3Keys);

        if (files.size() != s3Keys.size()) {
            throw new BadRequestException(ResponseCode.NOT_FOUND_AWS_FILE);
        }

        for (AwsFileReference file : files) {
            file.setUsedIn(usedIn);
            file.setReferencePk(referencePk);
        }

        awsFileReferenceRepository.saveAll(files);
    }

    public List<AwsFileReference> findUnusedOlderThan3Days() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(2);
        return awsFileReferenceRepository.findByReferencePkIsNullAndCreatedAtBefore(threeDaysAgo);
    }

    public void delete(AwsFileReference file) {
        awsFileReferenceRepository.delete(file);

    }

    public AwsFileReference findByKey(String key){
        return awsFileReferenceRepository.findByS3Key(key);
    }

    @Transactional
    public void save(AwsFileReference awsFileReference) {
        awsFileReferenceRepository.save(awsFileReference);
    }

    public List<AwsFileReference> findbyBoardPk(Long boardPk) {
        return awsFileReferenceRepository.findByUsedInEqualsAndReferencePkEquals(UsedInType.BOARD, boardPk);
    }

    public List<AwsFileReference> findByEventBoardPk(Long eventBoardPk) {
        return awsFileReferenceRepository.findByUsedInEqualsAndReferencePkEquals(UsedInType.EVENT_BOARD, eventBoardPk);
    }

    public List<AwsFileReference> findByUserPk(Long userPk) {
        return awsFileReferenceRepository.findByUsedInEqualsAndReferencePkEquals(UsedInType.USER_PROFILE, userPk);
    }

    public List<AwsFileReference> findbyNewsPk(Long newsPk) {
        return awsFileReferenceRepository.findByUsedInEqualsAndReferencePkEquals(UsedInType.NEWS, newsPk);
    }

    public List<AwsFileReference> findbyBoardReplyPk(Long boardReplyPk) {
        return awsFileReferenceRepository.findByUsedInEqualsAndReferencePkEquals(UsedInType.BOARD_REPLY, boardReplyPk);
    }

    public List<AwsFileReference> findbyNewsReplyPk(Long newsReplyPk) {
        return awsFileReferenceRepository.findByUsedInEqualsAndReferencePkEquals(UsedInType.NEWS_REPLY, newsReplyPk);
    }

}
