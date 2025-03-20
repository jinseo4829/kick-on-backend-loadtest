package kr.kickon.api.domain.boardReplyKick.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateBoardReplyKickRequestDTO {
    @Positive
    @NotNull(message = "댓글 pk 값은 필수값입니다.")
    private Long reply;
}
