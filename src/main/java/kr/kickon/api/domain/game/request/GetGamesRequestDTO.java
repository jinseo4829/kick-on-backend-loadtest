package kr.kickon.api.domain.game.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetGamesRequestDTO {
    @NotNull
    @Positive
    private Long league;

    @NotNull
    @Positive
    private Integer season;

    @NotNull
    @Pattern(regexp = "proceeding|finished", message = "허용되지 않는 상태값입니다.")
    private String status;
}
