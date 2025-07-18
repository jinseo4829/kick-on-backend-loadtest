package kr.kickon.api.domain.boardViewHistory.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateBoardViewHistoryRequest {
    @Positive(message = "게시글 PK는 1이상의 정수입니다.")
    @NotNull(message = "게시글 pk 값은 필수값입니다.")
    @Schema(description = "게시글 pk 값", example = "1")
    private Long board;
}
