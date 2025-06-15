package kr.kickon.api.admin.report.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.kickon.api.global.common.enums.ReportStatus;
import lombok.Data;

@Data
@Schema(name = "UpdateReportStatusRequest", description = "신고 상태 수정 요청")
public class UpdateReportStatusRequest {

    @Schema(description = "신고 타입 (BOARD 또는 NEWS)", example = "BOARD", required = true)
    @NotBlank
    private String type;

    @Schema(description = "신고 PK", example = "123", required = true)
    @NotNull
    private Long reportPk;

    @Schema(description = "변경할 신고 상태", example = "PROCESSED", required = true)
    @NotNull
    private ReportStatus reportStatus;
}