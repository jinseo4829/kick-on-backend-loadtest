package kr.kickon.api.domain.boardReply.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetBoardRepliesRequest {
    @Positive(message = "게시글 PK는 1이상의 정수입니다.")
    @NotNull(message = "게시글 pk 값은 필수입니다.")
    @Schema(description = "게시글 pk 값", example = "1")
    private Long board;

    @NotNull(message = "size는 필수입니다.")
    @Positive(message = "size는 양수로 입력해주세요.")
    @Schema(description = "한 페이지 당 게시글 수", example = "10")
    private Integer size;

    @NotNull(message = "page는 필수입니다.")
    @Positive(message = "page는 양수로 입력해주세요.")
    @Schema(description = "이동할 페이지", example = "1")
    private Integer page;

    @Schema(description = "true면 무한스크롤 방식 (hasNext 반환됨), false거나 null이면 일반 페이지네이션 방식", example = "false")
    private Boolean infinite;

    @Schema(description = "마지막 게시글 게시글 pk", example = "1")
    @Positive(message = "게시글 pk는 양수로 입력해주세요.")
    private Long lastReply;
}
