package kr.kickon.api.domain.shorts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.entities.User;
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
public class ShortsDetailDTO extends ShortsDTO{

  @Schema(description = "댓글 수", example = "10")
  private Long replyCount;

  @Schema(description = "유저 정보")
  private BaseUserDTO user;

  public static ShortsDetailDTO fromEntity(VideoResource video, Long viewCount, Long kickCount, Long replyCount, String title, User user) {

    return ShortsDetailDTO.builder()
        .pk(video.getPk())
        .videoUrl(video.getVideoUrl())
        .usedIn(video.getUsedIn())
        .referencePk(video.getReferencePk())
        .title(title)
        .viewCount(viewCount)
        .kickCount(kickCount)
        .replyCount(replyCount)
        .createdAt(video.getCreatedAt())
        .user(user != null ? new BaseUserDTO(user) : null)
        .build();
  }
}
