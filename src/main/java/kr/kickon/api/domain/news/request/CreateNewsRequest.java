package kr.kickon.api.domain.news.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.enums.ValidEnum;
import kr.kickon.api.global.common.enums.NewsCategory;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class CreateNewsRequest {
    @Positive(message = "팀 pk값은 양수입니다.")
    @Schema(description = "팀 pk 값입니다. nullable", example = "1")
    private Long team;

    @NotNull(message = "뉴스 제목은 필수값입니다.")
    @Length(min = 1, max = 100, message = "뉴스 제목은 100자 이하로 작성해야합니다.")
    @Schema(description = "뉴스 제목", example = ExampleConstants.title)
    private String title;

    @NotNull(message = "뉴스 본문은 필수값입니다.")
    @Schema(description = "뉴스 본문입니다.", example = ExampleConstants.content)
    private String contents;

    @Schema(description = "뉴스 썸네일입니다.", example = ExampleConstants.eventBoardImageUrl)
    private String thumbnailUrl;

    @NotNull(message = "뉴스 카테고리는 필수값입니다.")
    @ValidEnum(enumClass = NewsCategory.class, message = "허용되지 않는 카테고리입니다.")
    private NewsCategory category = NewsCategory.ETC;

    @Schema(description = "게시글 내 사용된 이미지 S3 키 배열", example = "[\"user1-2025-05-01-10-00-00.png\"]")
    private String[] usedImageKeys;

    @Schema(description = "게시글 내 사용된 영상 S3 키 배열", example = "[\"화면_기록_20250528_오후_11727_20250601002627.mov\"]")
    private String[] usedVideoKeys;

    @Schema(description = "게시글 내 삽입된 영상 링크 배열", example = "[\"https://www.youtube.com/watch?v=szWUXa7NI0s\"]")
    private String[] embeddedLinks;
}
