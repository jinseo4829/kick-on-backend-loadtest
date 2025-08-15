package kr.kickon.api.domain.shorts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "쇼츠 상세 DTO")
public class ShortsDetailDTO extends ShortsDTO {

  @Schema(description = "댓글 수", example = "10")
  private Long replyCount;

  @Schema(description = "유저 정보")
  private BaseUserDTO user;

}
