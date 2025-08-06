package kr.kickon.api.domain.news.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.News;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class NewsDetailDTO extends NewsListDTO {
    @Schema(example = "[\"user1-2025-05-01-10-00-00.png\"]", description = "게시글 내 사용된 이미지 S3 키 배열")
    private String[] usedImageKeys;

    @Schema(example = "[\"https://www.youtube.com/watch?v=szWUXa7NI0s\"]", description = "게시글 내 삽입된 영상 링크 배열")
    private String[] embeddedLinks;

    @Schema(example = "true", description = "킥 눌렀는지 여부")
    private Boolean isKicked;

    public NewsDetailDTO(News news, BaseUserDTO user, Integer likes, Integer views,Integer replies) {
        super(news, user, likes, views, replies);
    }
}
