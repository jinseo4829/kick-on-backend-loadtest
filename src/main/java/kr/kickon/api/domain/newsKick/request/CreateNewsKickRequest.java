package kr.kickon.api.domain.newsKick.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateNewsKickRequest {
    @Positive(message = "뉴스 pk 값은 양수입니다.")
    @NotNull(message = "뉴스 pk 값은 필수값입니다.")
    @Schema(description = "뉴스 pk 값", example = "2")
    private Long news;
}
