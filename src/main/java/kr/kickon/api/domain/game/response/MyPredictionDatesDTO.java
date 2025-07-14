package kr.kickon.api.domain.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "내가 참여한 경기 날짜 리스트")
public class MyPredictionDatesDTO {
    @Schema(description = "내가 참여한 경기 날짜 리스트", example = "[2025-07-03, 2025-07-08]")
    private List<LocalDate> dates;
}
