package kr.kickon.api.domain.newsReplyKick.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateNewsReplyKickRequest {
    @Positive
    @NotNull(message = "댓글 pk 값은 필수값입니다.")
    private Long reply;
}
