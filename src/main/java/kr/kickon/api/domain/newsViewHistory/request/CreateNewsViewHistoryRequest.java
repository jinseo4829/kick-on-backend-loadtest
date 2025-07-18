package kr.kickon.api.domain.newsViewHistory.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateNewsViewHistoryRequest {
    @Positive
    @NotNull(message = "뉴스 pk 값은 필수값입니다.")
    private Long news;
}
