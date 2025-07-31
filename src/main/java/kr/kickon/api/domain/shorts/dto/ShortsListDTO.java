package kr.kickon.api.domain.shorts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.AwsFileReference;
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
public class ShortsListDTO extends ShortsDTO{

  @Schema(description = "작성일", example = ExampleConstants.datetime)
  private LocalDateTime createdAt;

  public static ShortsListDTO fromEntitay(
      AwsFileReference file,
      Long viewCount,
      Long kickCount,
      String title) {
    String trimmedS3Key = file.getS3Key()
        .replaceFirst("^dev/board-files/", "")
        .replaceFirst("^dev/news-files/", "")
        .replaceFirst("^local/board-files/", "")
        .replaceFirst("^local/news-files/", "");

    return ShortsListDTO.builder()
        .pk(file.getPk())
        .s3Key(trimmedS3Key)
        .usedIn(file.getUsedIn())
        .referencePk(file.getReferencePk())
        .title(title)
        .viewCount(viewCount)
        .kickCount(kickCount)
        .createdAt(file.getCreatedAt())
        .build();
  }

}
