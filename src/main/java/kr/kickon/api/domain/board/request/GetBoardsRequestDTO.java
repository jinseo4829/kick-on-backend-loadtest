package kr.kickon.api.domain.board.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class GetBoardsRequestDTO {
    @Positive
    private Long team;

    @NotNull(message = "size는 필수입니다.")
    @Positive(message = "size는 양수로 입력해주세요.")
    private Integer size;

    @NotNull(message = "page는 필수입니다.")
    @Positive(message = "page는 양수로 입력해주세요.")
    private Integer page;
}
