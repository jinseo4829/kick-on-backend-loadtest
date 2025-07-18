package kr.kickon.api.domain.board.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class GetBoardsRequest {
    @Positive(message = "팀 pk 값은 양수로 입력해주세요.")
    @Schema(description = "팀 pk 값, nullable", example = "2")
    private Long team;

    @NotNull(message = "size는 필수입니다.")
    @Positive(message = "size는 양수로 입력해주세요.")
    @Schema(description = "한 페이지 당 게시글 수", example = "10")
    private Integer size;

    @NotNull(message = "page는 필수입니다.")
    @Positive(message = "page는 양수로 입력해주세요.")
    @Schema(description = "이동하고 싶은 페이지", example = "2")
    private Integer page;

    @NotNull(message = "정렬 기준은 필수입니다.")
    @Pattern(regexp = "hot|recent", message = "허용되지 않는 정렬 기준입니다.")
    @Schema(description = "정렬 기준, 필수값으로 default는 recent, hot | recent 가능 / hot이면 인기 게시글 / team이랑 같이 사용 불가", example = "2")
    private String order;

    @Schema(description = "true면 무한스크롤 방식 (hasNext 반환됨), false거나 null이면 일반 페이지네이션 방식", example = "false")
    private Boolean infinite;

    @Schema(description = "마지막 게시글 게시글 pk", example = "true")
    @Positive(message = "게시글 pk는 양수로 입력해주세요.")
    private Long lastBoard;

    @Schema(description = "마지막 게시글의 view Count", example = "123")
    private Long lastViewCount;
}
