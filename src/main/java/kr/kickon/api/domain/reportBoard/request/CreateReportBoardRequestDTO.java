package kr.kickon.api.domain.reportBoard.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class CreateReportBoardRequestDTO {
    @Positive(message = "게시글 pk 값은 1이상의 정수로 입력해주세요")
    @NotNull(message = "게시글 pk 값은 필수값입니다.")
    @Schema(description = "게시글 PK 값", example = "1")
    private Long board;

    @Length(min = 1,max = 500, message = "게시글 신고 사유는 500자 미만으로 입력해주세요.")
    @Schema(description = "신고 사유", example = "비방 및 욕설 표현을 사용했어요.")
    private String reason;
}
