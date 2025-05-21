package kr.kickon.api.domain.awsFileReference;

import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.enums.UsedInType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface AwsFileReferenceRepository extends JpaRepository<AwsFileReference, Long>, QuerydslPredicateExecutor<AwsFileReference> {
    List<AwsFileReference> findByReferencePkIsNullAndCreatedAtBefore(LocalDateTime threshold);
    List<AwsFileReference> findAllByS3KeyIn(List<String> s3Keys);
    List<AwsFileReference> findByUsedInEqualsAndReferencePkEquals(UsedInType usedIn, Long referencePk);
    AwsFileReference findByS3Key(String s3Key);

}
