package kr.kickon.api.domain.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "내 응원팀 기준 달력 일정 + 경기 수 응답 DTO")
public class CalendarDateCountResponse {

    @Schema(description = "날짜별 경기 리스트")
    private List<CalendarDateCountDTO> dates;
}

