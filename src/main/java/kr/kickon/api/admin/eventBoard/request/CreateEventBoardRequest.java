package kr.kickon.api.admin.eventBoard.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "이벤트 배너 생성 Request")
public class CreateEventBoardRequest {
    @NotBlank
    @Size(max = 100)
    @Schema(description = "배너 제목", example = "손흥민 이벤트 배너")
    private String title;

    @NotBlank
    @Schema(description = "썸네일 S3 file URL", example = ExampleConstants.thumbnail)
    private String thumbnailUrl;

    @NotBlank
    @Schema(description = "임베드 URL", example = "https://naver.com")
    private String embeddedUrl;
}