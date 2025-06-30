package kr.kickon.api.admin.gambleSeason.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@Schema(description = "승부 예측 시즌 리스트 조회를 위한 필터링 요청 객체")
public class GambleSeasonFilterRequest {

  @Schema(description = "리그pk", example = "1")
  private Long leaguePk;

  @Schema(description = "시작일", example = "2025-03-16T12:00:00")
  private String startedAt;

  @Schema(description = "마감일", example = "2025-06-16T12:00:00")
  private String finishedAt;

  @Schema(description = "상태 값", example = "PROCEEDING")
  private String operatingStatus;

  @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
  private Integer page = 1;

  @Schema(description = "페이지 크기", example = "20")
  private Integer size = 20;

  public Pageable toPageable() {
    int pageIndex = Math.max(this.page - 1, 0); // 0-based
    return PageRequest.of(pageIndex, size);
  }
}