package kr.kickon.api.domain.newsReply.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class CreateNewsReplyRequestDTO {
    @Positive
    @NotNull(message = "뉴스 pk 값은 필수값입니다.")
    private Long news;

    @Positive
    private Long parentReply;

    @NotNull(message = "댓글 본문은 필수값입니다.")
    @Length(min = 1, max = 1000,message = "댓글은 1000자 이하로 작성해주세요.")
    private String contents;
}
