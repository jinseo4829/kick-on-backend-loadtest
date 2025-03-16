package kr.kickon.api.domain.news.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import kr.kickon.api.global.common.annotations.ValidEnum;
import kr.kickon.api.global.common.enums.NewsCategory;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class CreateNewsDTO {
    @Positive
    private Long team;

    @NotNull
    @Length(min = 1, max = 100)
    private String title;

    @NotNull
    private String contents;

    private String thumbnailUrl;

    @NotNull
    @ValidEnum(enumClass = NewsCategory.class, message = "허용되지 않는 카테고리입니다.")
    private NewsCategory category = NewsCategory.ETC;
}
