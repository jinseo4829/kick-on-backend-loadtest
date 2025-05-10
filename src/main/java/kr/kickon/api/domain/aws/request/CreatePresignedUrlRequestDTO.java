package kr.kickon.api.domain.aws.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePresignedUrlRequestDTO {
    @NotNull(message = "타입은 null일 수 없습니다.")
    @Pattern(regexp = "profile-images|board-files|news-files|board-reply-files|news-reply-files", message = "허용되지 않는 타입입니다.")
    @Schema(description = "어떤 곳에서 쓰이는지 저장되는 type, 프로필 사진 : profile-images / 커뮤니티 글 내 사진 : board-files / 뉴스 글 내 사진 : news-files ", example = "news-files")
    private String type = "board-files";

    @NotNull(message = "저장될 파일 이름은 null일 수 없습니다.")
    @Schema(description = "저장될 파일 이름", example = ExampleConstants.id + "-2025-03-21-17-00-00.png")
    private String fileName;
}
