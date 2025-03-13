package kr.kickon.api.domain.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "게시글 리스트 조회를 위한 게시글 DTO")
public class BoardListDTO {
    @Schema(example = "1", description = "게시글 pk값")
    private Long pk;

    @Schema(example = ExampleConstants.title, description = "게시글 제목")
    private String title;

    private UserDTO user;

    @Schema(example = ExampleConstants.datetime, description = "생성 시각")
    private LocalDateTime createdAt;

    @Schema(example = "1233", description = "조회수")
    private Integer views;

    @Schema(example = "12354", description = "킥수")
    private Integer likes;

    @Schema(example = "12354", description = "댓글 수")
    private Integer replies;
}
