package kr.kickon.api.domain.news.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.MappedSuperclass;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.enums.NewsCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@Schema(description = "뉴스 리스트 조회를 위한 뉴스 DTO")
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class NewsListDTO {
    @Schema(example = "1", description = "뉴스 pk값")
    private Long pk;

    @Schema(example = ExampleConstants.title, description = "뉴스 제목")
    private String title;

    @Schema(example = ExampleConstants.content, description = "뉴스 제목")
    private String content;

    @Schema(example = ExampleConstants.thumbnail, description = "뉴스 썸네일")
    private String thumbnailUrl;

    @Schema(example = NewsCategory.VALUE.RENEWAL, description = "뉴스 카테고리")
    private String category;

    private UserDTO user;

    private TeamDTO team;

    @Schema(example = ExampleConstants.datetime, description = "생성 시각")
    private LocalDateTime createdAt;

    @Schema(example = "1233", description = "조회수")
    private Integer views;

    @Schema(example = "12354", description = "킥수")
    private Integer likes;

    @Schema(example = "12354", description = "댓글 수")
    private Integer replies;
}
