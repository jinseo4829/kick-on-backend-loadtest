package kr.kickon.api.admin.partners.request;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@Schema(description = "파트너스 리스트 조회를 위한 필터링 요청 객체")
public class PartnersFilterRequest {

  @Schema(description = "이름", example = ExampleConstants.name)
  private String name;

  @Schema(description = "닉네임", example = ExampleConstants.nickname)
  private String nickname;

  @Schema(description = "리그pk", example = ExampleConstants.leaguePk)
  private Long leaguePk;

  @Schema(description = "팀pk", example = ExampleConstants.teamPk)
  private Long teamPk;

  @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
  private Integer page = 1;

  @Schema(description = "페이지 크기", example = "20")
  private Integer size = 20;

  public Pageable toPageable() {
    int pageIndex = Math.max(this.page - 1, 0); // 0-based
    return PageRequest.of(pageIndex, size);
  }
}