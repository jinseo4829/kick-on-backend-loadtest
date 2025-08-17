package kr.kickon.api.domain.shorts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.UsedInType;
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

  @Schema(description = "킥 눌렀는지 여부", example = "true")
  private Boolean isKicked = false;

  public ShortsDetailDTO(Long pk, String videoUrl, UsedInType usedIn, Long referencePk,
      String title, Long totalViewCount, Long totalKickCount, Long totalReplyCount, LocalDateTime createdAt,
      User user, Boolean isKicked) {
    this.setPk(pk);
    this.setVideoUrl(videoUrl);
    this.setUsedIn(usedIn);
    this.setReferencePk(referencePk);
    this.setTitle(title);
    this.setViewCount(totalViewCount);
    this.setKickCount(totalKickCount);
    this.setCreatedAt(createdAt);
    this.setReplyCount(totalReplyCount);
    this.user = new BaseUserDTO(user);
    this.setIsKicked(isKicked);
  }
}
