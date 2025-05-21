package kr.kickon.api.domain.aws;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.aws.dto.PresignedUrlDTO;
import kr.kickon.api.domain.aws.request.CreatePresignedUrlRequestDTO;
import kr.kickon.api.domain.aws.response.CreatePresignedUrlResponseDTO;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.common.enums.UsedInType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/aws")
@Tag(name = "AWS 관련")
@Slf4j
public class AwsController {
    private final AwsService awsService;
    private final AwsFileReferenceService awsFileReferenceService;
    private final String bucket = "kickon-files-bucket";
    @Value("${spring.config.activate.on-profile}")
    private String env;

    @Operation(summary = "AWS S3 presigned URL 생성", description = "파일명 바탕으로 생성 / header에 x-amz-acl : public-read 추가 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = CreatePresignedUrlResponseDTO.class))),
    })
    @PostMapping("/presigned-url")
    public ResponseEntity<ResponseDTO<PresignedUrlDTO>> createPresignedUrl(@Valid @RequestBody CreatePresignedUrlRequestDTO createPresignedUrlRequestDTO) {
        String key = env + "/" + createPresignedUrlRequestDTO.getType() + "/" + createPresignedUrlRequestDTO.getFileName();
        String presignedUrl = awsService.createPresignedUrl(bucket,key);
        String s3Url = awsService.getS3Url(bucket,key);


        PresignedUrlDTO presignedUrlDTO = PresignedUrlDTO.builder()
                .presignedUrl(presignedUrl)
                .s3Url(s3Url)
                .build();

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED, presignedUrlDTO));
    }

    @Scheduled(cron = "0 0 3 * * *")
    @DeleteMapping("/s3-files")
    public void cleanUnusedS3Files(){
        awsService.cleanupUnusedFiles();
    }
}
