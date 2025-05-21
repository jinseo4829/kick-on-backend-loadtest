package kr.kickon.api.domain.aws;

import jakarta.transaction.Transactional;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.common.enums.UsedInType;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.error.exceptions.InternalServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsService{
    private final String bucket = "kickon-files-bucket";
    private final AwsFileReferenceService awsFileReferenceService;
    /* Create a presigned URL to use in a subsequent PUT request */
    public String createPresignedUrl(String bucketName, String keyName) {
        try (S3Presigner presigner = S3Presigner.create()) {

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(1))  // The URL expires in 10 minutes.
                    .putObjectRequest(objectRequest)
                    .build();


            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

            // 2. AwsFileReference DB 등록
            AwsFileReference awsFileReference = AwsFileReference.builder()
                    .id(UUID.randomUUID().toString())
                    .s3Key(keyName)
                    .usedIn(UsedInType.TEMP)
                    .build();
            awsFileReferenceService.save(
                    awsFileReference
            );
//            System.out.println(awsFileReference);

            return presignedRequest.url().toString();
        }catch (Exception e) {
            throw new InternalServerException(ResponseCode.AWS_PRESIGNED_ERROR, e.getMessage());
        }
    }

    public String getS3Url(String bucketName, String keyName) {
        S3Client s3 = S3Client.builder().build();
        GetUrlRequest request = GetUrlRequest.builder().bucket(bucketName).key(keyName).build();

        URL url = s3.utilities().getUrl(request);
        return url.toString();
    }

    @Transactional
    public void cleanupUnusedFiles() {
        List<AwsFileReference> unusedFiles = awsFileReferenceService.findUnusedOlderThan3Days();
//        System.out.println(unusedFiles);
        if (unusedFiles.isEmpty()) {
            return;
        }

        try (S3Client s3 = S3Client.builder().build()) {
            for (AwsFileReference file : unusedFiles) {
                deleteFileFromS3AndDb(s3, file);
            }
        }
    }

    @Transactional
    public void deleteFileFromS3AndDb(S3Client s3, AwsFileReference file) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(file.getS3Key())
                .build());
            awsFileReferenceService.delete(file);
        } catch (Exception e) {
            System.out.println(e);
            throw new InternalServerException(ResponseCode.INTERNAL_SERVER_ERROR, e.getCause());
        }
    }
}
