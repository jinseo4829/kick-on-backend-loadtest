package kr.kickon.api.domain.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Schema(description = "달력 날짜 + 경기 수 DTO")
public class CalendarDateCountDTO {

    @Schema(description = "경기 있는 날짜", example = "2025-07-20")
    private LocalDate date;

    @Schema(description = "해당 날짜의 경기 수", example = "3")
    private int count;
}
