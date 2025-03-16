package kr.kickon.api.domain.board.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class CreateBoardRequestDTO {
    @Positive
    private Long team;

    @NotNull
    @Length(min = 1, max = 100)
    private String title;

    @NotNull
    private String contents;
}
