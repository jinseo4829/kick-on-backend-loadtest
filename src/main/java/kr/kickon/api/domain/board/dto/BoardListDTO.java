package kr.kickon.api.domain.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.MappedSuperclass;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@Schema(description = "게시글 리스트 조회를 위한 게시글 DTO")
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class BoardListDTO {
    @Schema(example = "1", description = "게시글 pk값")
    private Long pk;

    @Schema(example = ExampleConstants.title, description = "게시글 제목")
    private String title;

    private BaseUserDTO user;

    private TeamDTO team;

    @Schema(example = ExampleConstants.datetime, description = "생성 시각")
    private LocalDateTime createdAt;

    @Schema(example = "true", description = "게시글 이미지 포함 여부")
    private Boolean hasImage;

    @Schema(example = "1233", description = "조회수")
    private Integer views;

    @Schema(example = "12354", description = "킥수")
    private Integer likes;

    @Schema(example = "12354", description = "댓글 수")
    private Integer replies;

    @Schema(description = "인플루언서 여부", example = "false")
    private Boolean isInfluencer;
}
