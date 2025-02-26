package kr.kickon.api.domain.eventBoard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 추가 필요!
public class GetEventBoardDTO{
    @Schema(description = "ID", example = "fbfa7463-8315-472a-84ae-594697296719")
    private String id;

    @Schema(description = "게시글 이름", example = "2024-05-02-맨시티-리버풀-손흥민-메인")
    private String title;

    @Schema(description = "사진 url", example = "https://localhost:8080/image.url")
    private String thumbnailUrl;

    @Schema(description = "사진 url", example = "https://localhost:8080/image.url")
    private String embeddedUrl;

    @Schema(description = "display 순서", example = "1")
    private Integer orderNum;

    @Override
    public String toString() {
        return "GetEventBoardDTO [id=" + id + ", title=" + title + ", thumbnailUrl=" + thumbnailUrl + ", embeddedUrl=" + embeddedUrl + ", orderNum=" + orderNum + "]";
    }
}
