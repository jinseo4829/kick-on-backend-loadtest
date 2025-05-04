package kr.kickon.api.domain.awsFileReference;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.common.enums.UsedInType;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        return awsFileReferenceRepository.findByReferencePkIsNullAndCreatedAtBefore(threeDaysAgo);
    }

    public void delete(AwsFileReference file) {
        awsFileReferenceRepository.delete(file);
    }

    public void save(AwsFileReference awsFileReference) {
        awsFileReferenceRepository.save(awsFileReference);
    }
}
