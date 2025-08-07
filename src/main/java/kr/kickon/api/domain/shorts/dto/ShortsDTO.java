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

  @Schema(description = "s3Key", example = ExampleConstants.s3Key)
  private String s3Key;

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

  public static ShortsDTO fromEntity(AwsFileReference file, Long viewCount, Long kickCount, String title) {

    return ShortsDTO.builder()
        .pk(file.getPk())
        .s3Key(file.getS3Key())
        .usedIn(file.getUsedIn())
        .referencePk(file.getReferencePk())
        .title(title)
        .viewCount(viewCount)
        .kickCount(kickCount)
        .createdAt(file.getCreatedAt())
        .build();
  }
}
