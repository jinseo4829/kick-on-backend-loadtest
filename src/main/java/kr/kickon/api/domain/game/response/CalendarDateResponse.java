package kr.kickon.api.domain.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "특정 월에 경기 일정이 있는 날짜 리스트를 반환하는 DTO")
public class CalendarDateResponse {

    @Schema(description = "경기 일정이 있는 날짜 리스트", example = "[\"2025-07-10\", \"2025-07-12\", \"2025-07-20\"]")
    private List<LocalDate> dates;
}
