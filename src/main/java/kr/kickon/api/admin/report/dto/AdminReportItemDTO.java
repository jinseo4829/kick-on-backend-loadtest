package kr.kickon.api.admin.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(name = "AdminReportItemDTO", description = "신고 리스트 응답")
public class AdminReportItemDTO {
    @Schema(description = "신고 대상의 PK (게시글 또는 뉴스)", example = "123")
    private Long pk;

    @Schema(description = "신고 대상 타입", example = "BOARD") // or NEWS
    private String type;

    @Schema(description = "작성자 유저 PK", example = "456")
    private Long userPk;

    @Schema(description = "작성자 닉네임", example = "soccerFan01")
    private String nickname;

    @Schema(description = "게시글 또는 뉴스 제목", example = "논란이 된 경기 리뷰")
    private String title;

    @Schema(description = "신고 누적 횟수", example = "5")
    private Long reportCount;

    @Schema(description = "게시글 생성시각", example = "2025-06-15T14:22:00")
    private LocalDateTime createdAt;
}