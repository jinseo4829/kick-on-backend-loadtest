package kr.kickon.api.admin.report;

import com.mysema.commons.lang.Pair;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.admin.game.response.GetGamesResponse;
import kr.kickon.api.admin.report.dto.AdminReportDetailDTO;
import kr.kickon.api.admin.report.dto.AdminReportItemDTO;
import kr.kickon.api.admin.report.request.UpdateReportStatusRequest;
import kr.kickon.api.admin.report.response.GetTargetReportsResponse;
import kr.kickon.api.domain.reportBoard.ReportBoardService;
import kr.kickon.api.domain.reportNews.ReportNewsService;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/report")
@Tag(name = "신고")
@Slf4j
public class AdminReportController {
    private final ReportBoardService reportBoardService;
    private final ReportNewsService reportNewsService;
    private final AdminReportService adminReportService;

    @Operation(summary = "신고 통합 리스트 조회", description = "게시글/뉴스 통합 신고 리스트를 필터 및 정렬 조건에 따라 조회," +
            "\n type은 \"BOARD\", \"NEWS\", \"ALL\" 중 하나" +
            "\n sort는 \"REPORT_COUNT\", \"CREATED_AT\" 중 하나")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetGamesResponse.class))),
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<List<AdminReportItemDTO>>> getReports(
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "REPORT_COUNT") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // ✅ type 유효성 검사
        List<String> validTypes = List.of("BOARD", "NEWS");
        if (type!=null && !validTypes.contains(type.toUpperCase())) {
            throw new BadRequestException(ResponseCode.INVALID_REQUEST);
        }

        // ✅ sort 유효성 검사
        List<String> validSorts = List.of("REPORT_COUNT", "CREATED_AT");
        if (!validSorts.contains(sort.toUpperCase())) {
            throw new BadRequestException(ResponseCode.INVALID_REQUEST);
        }

        int offset = (page - 1) * size;

        // 데이터 조회
        List<AdminReportItemDTO> reports = adminReportService.getReports(type, sort, offset, size);
        long totalItems = adminReportService.countReports(type);

        // 응답 반환
        return ResponseEntity.ok(
                ResponseDTO.success(
                        ResponseCode.SUCCESS,
                        reports,
                        new PagedMetaDTO(page, size, totalItems)
                )
        );
    }

    @Operation(summary = "게시글별 신고 리스트 조회", description = "게시글/뉴스 통합 신고 리스트를 필터 및 정렬 조건에 따라 조회," +
            "\n type은 \"BOARD\", \"NEWS\" 중 하나" +
            "\n pk는 해당 게시글의 pk")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetTargetReportsResponse.class))),
    })
    @GetMapping("/target")
    public ResponseEntity<ResponseDTO<List<AdminReportDetailDTO>>> getReportsByPk(
            @RequestParam(required = false, defaultValue = "BOARD") String type,
            @RequestParam() Long pk,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){

        int offset = (page - 1) * size;
        Pair<List<AdminReportDetailDTO>, Long> result = adminReportService.getReportDetailsByTarget(type, pk, offset, size);
        return ResponseEntity.ok(
                ResponseDTO.success(
                        ResponseCode.SUCCESS,
                        result.getFirst(),
                        new PagedMetaDTO(page, size, result.getSecond())
                )
        );
    }

    @Operation(summary = "신고 상태 수정", description = "신고 PK 기준으로 신고 상태(reportStatus)를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "해당 신고 없음")
    })
    @PatchMapping("/status")
    public ResponseEntity<ResponseDTO<Void>> updateReportStatus(
            @RequestBody @Valid UpdateReportStatusRequest request
    ) {
        adminReportService.updateReportStatus(request);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

}