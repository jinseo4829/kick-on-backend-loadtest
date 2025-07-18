package kr.kickon.api.domain.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "내가 참여한 경기 날짜 리스트 + 경기 수 응답 DTO")
public class MyPredictionDatesResponse {

    @Schema(description = "날짜별 경기 리스트")
    private List<CalendarDateCountDTO> dates;
}
