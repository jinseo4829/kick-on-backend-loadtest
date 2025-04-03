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

    @Schema(example = "true", description = "킥 눌렀는지 여부")
    private Boolean isKicked = false;
}
