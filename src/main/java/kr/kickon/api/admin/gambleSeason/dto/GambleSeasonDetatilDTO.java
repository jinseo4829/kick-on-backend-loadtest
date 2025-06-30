package kr.kickon.api.admin.gambleSeason.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "승부 예측 시즌 상세 응답 DTO")
public class GambleSeasonDetatilDTO {

  @Schema(description = "승부 예측 시즌 PK", example = "1")
  private Long pk;

  @Schema(description = "리그", example = "PENDING")
  private League league;

  @Schema(description = "제목", example = "24/25 K 리그")
  private String title;

  @Schema(description = "시작일", example = "2025-03-16T12:00:00")
  private LocalDateTime startedAt;

  @Schema(description = "마감일", example = "2025-06-16T12:00:00")
  private LocalDateTime finishedAt;

  @Schema(description = "상태", example = "PROCEEDING")
  private OperatingStatus operatingStatus;

  @Schema(description = "기타 정보", nullable = true)
  private String etc;
}