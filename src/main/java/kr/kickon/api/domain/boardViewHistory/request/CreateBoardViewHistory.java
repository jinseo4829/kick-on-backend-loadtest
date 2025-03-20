package kr.kickon.api.domain.boardViewHistory.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateBoardViewHistory {
    @Positive
    @NotNull(message = "게시글 pk 값은 필수값입니다.")
    private Long board;
}
