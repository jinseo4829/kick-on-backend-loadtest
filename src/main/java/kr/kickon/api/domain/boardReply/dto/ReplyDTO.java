package kr.kickon.api.domain.boardReply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.board.dto.UserDTO;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "댓글 DTO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDTO {
    @Schema(example = "1", description = "댓글 PK")
    private Long pk;
    @Schema(example = ExampleConstants.replyContent, description = "댓글 본문")
    private String contents;
    private UserDTO user;
    @Schema(example = ExampleConstants.datetime, description = "생성시각")
    private LocalDateTime createdAt;
    @Schema(example = "true", description = "내가 좋아요 했는지 여부")
    private boolean isKicked;
    @Schema(example = "12", description = "좋아요 수")
    private Long kickCount;
    private List<ReplyDTO> replies = new ArrayList<>();
}