package kr.kickon.api.domain.aws.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Builder;
import lombok.Data;

@Schema(description = "presigned url DTO")
@Builder
@Data
public class PresignedUrlDTO {
    @Schema(description = "업로드해야하는 presigned url", example = ExampleConstants.profileImageUrl)
    private String presignedUrl;

    @Schema(example = ExampleConstants.profileImageUrl, description = "저장 되는 s3 url")
    private String s3Url;
}
