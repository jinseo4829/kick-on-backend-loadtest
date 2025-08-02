package kr.kickon.api.domain.boardReplyKick.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateBoardReplyKickRequestDTO {
    @Positive(message = "댓글 PK는 1이상의 정수입니다.")
    @NotNull(message = "댓글 pk 값은 필수값입니다.")
    @Schema(description = "댓글 pk", example = "10")
    private Long reply;
}
