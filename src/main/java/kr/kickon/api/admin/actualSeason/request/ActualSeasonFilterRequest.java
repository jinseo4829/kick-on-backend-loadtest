package kr.kickon.api.admin.actualSeason.request;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@Schema(description = "실제 시즌 리스트 조회를 위한 필터링 요청 객체")
public class ActualSeasonFilterRequest {

  @Schema(description = "리그pk", example = ExampleConstants.leaguePk)
  private Long leaguePk;

  @Schema(description = "상태 값", example = ExampleConstants.operatingStatus)
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
