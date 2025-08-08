package kr.kickon.api.domain.shorts.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.AwsFileReference;
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
@Schema(description = "쇼츠 리스트 DTO")
public class ShortsDTO {

  @Schema(description = "AwsFileReference pk", example = "215")
  private Long pk;

  @Schema(description = "videoUrl", example = ExampleConstants.s3Key)
  private String videoUrl;

  @Schema(description = "게시글/뉴스 구분", example = ExampleConstants.UsedInType)
  private UsedInType usedIn;

  @Schema(description = "게시글/뉴스 pk", example = "1")
  private Long referencePk;

  @Schema(description = "게시글/뉴스 제목", example = ExampleConstants.title)
  private String title;

  @Schema(description = "조회수", example = "10")
  private Long viewCount;

  @Schema(description = "킥 수", example = "10")
  private Long kickCount;

  @JsonIgnore
  private Long sortViewCount;

  @JsonIgnore
  private Long sortKickCount;

  @Schema(description = "작성일", example = ExampleConstants.datetime)
  private LocalDateTime createdAt;

  public interface VideoResource {
    Long getPk();
    String getVideoUrl();
    UsedInType getUsedIn();
    Long getReferencePk();
    LocalDateTime getCreatedAt();
  }

  public static ShortsDTO fromEntity(VideoResource video, Long viewCount, Long kickCount, String title) {

    return ShortsDTO.builder()
        .pk(video.getPk())
        .videoUrl(video.getVideoUrl())
        .usedIn(video.getUsedIn())
        .referencePk(video.getReferencePk())
        .title(title)
        .viewCount(viewCount)
        .kickCount(kickCount)
        .createdAt(video.getCreatedAt())
        .build();
  }
}
