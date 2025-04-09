package kr.kickon.api.domain.newsReply.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetNewsRepliesRequestDTO {
    @Positive(message = "뉴스 pk 값은 양수로 입력해주세요.")
    @NotNull(message = "뉴스 pk 값은 필수입니다.")
    private Long news;

    @NotNull(message = "size는 필수입니다.")
    @Positive(message = "size는 양수로 입력해주세요.")
    @Schema(description = "한 페이지 당 게시글 수", example = "10")
    private Integer size;

    @NotNull(message = "page는 필수입니다.")
    @Positive(message = "page는 양수로 입력해주세요.")
    @Schema(description = "이동할 페이지", example = "1")
    private Integer page;
}
