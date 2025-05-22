package kr.kickon.api.admin.game.request;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.enums.GameStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Schema(description = "게임 리스트 조회를 위한 필터링 요청 객체")
public class GameFilterRequest {
    @Schema(description = "팀 PK", example = "1")
    @Min(value = 1, message = "팀 PK는 1 이상의 값이어야 합니다.")
    private Long teamPk;

    @Schema(description = "게임 상태 목록", example = "[\"PENDING\", \"DRAW\"]")
    private List<GameStatus> gameStatuses;

    @Schema(description = "시즌 연도", example = "2024")
    @Min(value = 2000, message = "연도는 2000년 이후여야 합니다.")
    private Integer year;

    @Schema(description = "리그 PK", example = "3")
    @Min(value = 1, message = "리그 PK는 1 이상의 값이어야 합니다.")
    private Long leaguePk;

    @Schema(description = "경기 시작일 범위 시작", example = "2024-01-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startedFrom;

    @Schema(description = "경기 시작일 범위 종료", example = "2024-12-31T23:59:59")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startedTo;
}