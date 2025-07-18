package kr.kickon.api.domain.newsReply.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class PatchNewsReplyRequest {
  @NotNull(message = "댓글 본문은 필수값입니다.")
  @Length(min = 1, max = 1000, message = "댓글은 1000자 이하로 작성해주세요.")
  @Schema(description = "댓글 내용", example = ExampleConstants.replyContent)
  private String contents;

  @Schema(description = "게시글 내 사용된 이미지 S3 키 배열", example = "[\"user1-2025-05-01-10-00-00.png\"]")
  private String[] usedImageKeys;
}
