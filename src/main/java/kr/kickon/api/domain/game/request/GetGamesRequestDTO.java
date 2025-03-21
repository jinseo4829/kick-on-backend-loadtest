package kr.kickon.api.domain.game.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetGamesRequestDTO {
    @NotNull(message = "리그 pk 값은 필수값입니다.")
    @Positive(message = "리그 pk 값은 양수입니다.")
    @Schema(description = "리그 pk 값", example = "2")
    private Long league;

    @NotNull(message = "시즌은 필수값입니다.")
    @Positive(message = "시즌 값은 양수입니다.")
    @Schema(description = "시즌 값입니다.", example = "2024")
    private Integer season;

    @NotNull(message = "status는 필수값입니다.")
    @Pattern(regexp = "proceeding|finished", message = "허용되지 않는 상태값입니다.")
    @Schema(description = "status 값입니다. proceeding | finished / proceeding이면 아직 승부예측 진행중인거, finished면 승부예측 끝난거", example = "proceeding")
    private String status;
}
