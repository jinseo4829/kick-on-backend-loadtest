package kr.kickon.api.domain.team.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@Schema(description = "팀 리스트 필터 요청 DTO")
public class TeamListFilterRequest {

  @Schema(description = "리그 PK", example = "1")
  private Long league;

  @Schema(description = "검색 키워드 (한글 or 영어 팀명)", example = "울산")
  private String keyword;

  @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
  private Integer page = 1;

  @Schema(description = "페이지 크기", example = "20")
  private Integer size = 20;

  public Pageable toPageable() {
    int pageIndex = Math.max(this.page - 1, 0); // 0-based
    return PageRequest.of(pageIndex, size);
  }
}