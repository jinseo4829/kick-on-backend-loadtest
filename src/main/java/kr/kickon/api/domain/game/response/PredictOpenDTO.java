package kr.kickon.api.domain.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Schema(description = "승부예측 오픈된 기간 DTO")
public class PredictOpenDTO {

    @Schema(description = "승부예측 시작일 (일요일 기준)", example = "2025-07-06")
    private LocalDate startDate;

    @Schema(description = "승부예측 종료일 (4주 후 토요일)", example = "2025-08-02")
    private LocalDate endDate;

    @Schema(description = "오픈된 주차 수", example = "4")
    private int weeks;
}
