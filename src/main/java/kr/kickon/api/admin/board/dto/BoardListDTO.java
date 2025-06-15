package kr.kickon.api.admin.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.MappedSuperclass;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@Schema(name = "AdminBoardDetailDTO", description = "게시글 리스트 조회를 위한 게시글 DTO")
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class BoardListDTO {
    @Schema(example = "1", description = "게시글 pk값")
    private Long pk;

    @Schema(example = ExampleConstants.title, description = "게시글 제목")
    private String title;

    @Schema(example = ExampleConstants.datetime, description = "생성 시각")
    private LocalDateTime createdAt;
}
