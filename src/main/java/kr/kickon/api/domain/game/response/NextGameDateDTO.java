package kr.kickon.api.domain.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Schema(description = "가장 가까운 예정 경기 날짜 DTO")
public class NextGameDateDTO {

    @Schema(description = "가장 가까운 예정 경기 날짜", example = "2025-07-03")
    private LocalDate nextDate;
}
