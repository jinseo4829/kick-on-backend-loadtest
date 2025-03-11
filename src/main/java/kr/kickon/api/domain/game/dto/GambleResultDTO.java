package kr.kickon.api.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "승부예측 현황 DTO")
public class GambleResultDTO {
    @Schema(description = "홈 선택 비율", example = "12")
    private Integer home;
    @Schema(description = "어웨이 선택 비율", example = "58")
    private Integer away;
    @Schema(description = "무승부 선택 비율", example = "30")
    private Integer draw;
    @Schema(description = "참여 인원", example = "1202")
    private Long participationNumber;
}
