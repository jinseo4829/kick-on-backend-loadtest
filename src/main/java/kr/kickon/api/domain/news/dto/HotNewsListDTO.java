package kr.kickon.api.domain.news.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.enums.NewsCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "많이 본 뉴스 리스트 조회를 위한 뉴스 DTO")
public class HotNewsListDTO {
    @Schema(example = "1", description = "뉴스 pk값")
    private Long pk;

    @Schema(example = ExampleConstants.title, description = "뉴스 제목")
    private String title;
    @Schema(example = ExampleConstants.thumbnail, description = "뉴스 썸네일")
    private String thumbnailUrl;

    @Schema(example = NewsCategory.VALUE.RENEWAL, description = "뉴스 카테고리")
    private NewsCategory category;

    @Schema(example = "1233", description = "조회수")
    private Integer views;
}
