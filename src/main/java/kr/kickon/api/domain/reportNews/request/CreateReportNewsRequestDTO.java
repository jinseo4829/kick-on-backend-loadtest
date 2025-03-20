package kr.kickon.api.domain.reportNews.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class CreateReportNewsRequestDTO {
    @Positive(message = "뉴스 pk는 1이상의 정수로 입력해주세요")
    @NotNull(message = "뉴스 pk 값은 필수값입니다.")
    @Schema(description = "뉴스 PK 값", example = "1")
    private Long news;

    @Length(min = 1,max = 500, message = "뉴스 신고사유는 500자 미만으로 입력해주세요.")
    @Schema(description = "신고 사유", example = "비방 및 욕설 표현을 사용했어요.")
    private String reason;
}
