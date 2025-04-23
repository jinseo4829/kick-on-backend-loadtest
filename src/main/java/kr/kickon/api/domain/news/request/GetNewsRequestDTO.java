package kr.kickon.api.domain.news.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetNewsRequestDTO {
    @Schema(description = "team PK, 옵셔널임, 넣으면 팀별 뉴스 조회", example = "1")
    @Positive(message = "팀 pk는 양수로 입력해주세요.")
    private Long team;

    @Schema(description = "페이징 할 때 한 페이지 당 게시글 수", example = "10")
    @NotNull(message = "size는 필수입니다.")
    @Positive(message = "size는 양수로 입력해주세요.")
    private Integer size;

    @Schema(description = "페이징 할 때 보고 싶은 페이지", example = "3")
    @NotNull(message = "page는 필수입니다.")
    @Positive(message = "page는 양수로 입력해주세요.")
    private Integer page;

    @Schema(description = "hot아니면 recent만 가능, 정렬 조건으로 인기 뉴스 볼 때 사용하셈", example = "hot")
    @NotNull(message = "정렬 기준은 필수입니다.")
    @Pattern(regexp = "hot|recent", message = "허용되지 않는 정렬 기준입니다.")
    private String order;

    @Schema(description = "league PK, 옵셔널임, 넣으면 검색 결과에 리그 조건 추가 됨", example = "1")
    private Long league;

    @Schema(description = "true면 무한스크롤 방식 (hasNext 반환됨), false거나 null이면 일반 페이지네이션 방식", example = "false")
    private Boolean infinite;

    @Schema(description = "마지막 게시글 뉴스 pk", example = "true")
    @Positive(message = "뉴스 pk는 양수로 입력해주세요.")
    private Long lastNews;

    @Schema(description = "마지막 뉴스의 view Count", example = "123")
    private Long lastViewCount;
}
