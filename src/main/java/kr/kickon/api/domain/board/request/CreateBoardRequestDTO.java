package kr.kickon.api.domain.board.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class CreateBoardRequestDTO {
    @Positive(message = "팀 PK는 1이상의 정수입니다.")
    @Schema(description = "팀 pk 값, nullable", example = "2")
    private Long team;

    @NotNull(message = "게시글 제목은 필수값입니다.")
    @Length(min = 1, max = 100)
    @Schema(description = "게시글 제목", example = ExampleConstants.title)
    private String title;

    @NotNull(message = "게시글 내용은 필수값입니다.")
    @Schema(description = "게시글 내용", example = ExampleConstants.content)
    private String contents;

    @NotNull(message = "게시글에 이미지 포함 여부를 알려주세요.")
    @Schema(description = "게시글 이미지 포함 여부", example = "false")
    private Boolean hasImage;

    @Schema(description = "게시글 내 사용된 이미지 S3 키 배열", example = "[\"user1-2025-05-01-10-00-00.png\"]")
    private String[] usedImageKeys;

    @Schema(description = "게시글 상단 고정 여부", example = "false", nullable = true)
    private Boolean isPinned;
}
