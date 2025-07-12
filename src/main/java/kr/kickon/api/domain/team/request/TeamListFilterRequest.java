package kr.kickon.api.domain.team.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "팀 리스트 필터 요청 DTO")
public class TeamListFilterRequest {

  @Schema(description = "리그 PK", example = "1")
  private Long league;

  @Schema(description = "검색 키워드 (한글 or 영어 팀명)", example = "울산")
  private String keyword;

}
