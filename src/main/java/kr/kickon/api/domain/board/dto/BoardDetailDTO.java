package kr.kickon.api.domain.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class BoardDetailDTO extends BoardListDTO{
    @Schema(example = ExampleConstants.content, description = "게시글 내용")
    private String content;

    @Schema(example = "[\"user1-2025-05-01-10-00-00.png\"]", description = "게시글 내 사용된 이미지 S3 키 배열")
    private String[] usedImageKeys;

    @Schema(example = "[\"https://www.youtube.com/watch?v=szWUXa7NI0s\"]", description = "게시글 내 삽입된 영상 링크 배열")
    private String[] embeddedLinks;

    @Schema(example = "true", description = "킥 눌렀는지 여부")
    private Boolean isKicked = false;

    @Schema(description = "인플루언서 여부", example = "false")
    private Boolean isInfluencer;
}
