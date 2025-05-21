package kr.kickon.api.admin.eventBoard.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "event 업데이트 Request")
public class UpdateEventBoardRequest {
    @Size(max = 100, message = "배너 제목은 100자 이하만 가능합니다.")
    @Schema(description = "배너 제목", example = "손흥민 이벤트 배너")
    private String title;

    @Schema(description = "썸네일", example = ExampleConstants.thumbnail)
    private String thumbnailUrl;

    @Schema(description = "임베드 url", example = "https://naver.com")
    private String embeddedUrl;

    @Min(value = 1, message = "순서는 1 이상의 값이어야 합니다.")
    @Schema(description = "순서", example = "1")
    private Integer orderNum;

    @Schema(description = "노출 여부", example = "true")
    private Boolean isDisplayed;
}