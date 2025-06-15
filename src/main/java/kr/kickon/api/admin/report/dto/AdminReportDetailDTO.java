package kr.kickon.api.admin.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "AdminReportDetailDTO", description = "신고 상세 항목")
public class AdminReportDetailDTO {

    @Schema(description = "신고 PK", example = "1001")
    private Long pk;

    @Schema(description = "신고자 유저 PK", example = "2002")
    private Long userPk;

    @Schema(description = "신고자 닉네임", example = "badPlayerHunter")
    private String nickname;

    @Schema(description = "신고 사유", example = "부적절한 내용 포함")
    private String reason;

    @Schema(description = "신고 상태", example = "PENDING") // ReportStatus
    private ReportStatus reportStatus;
}