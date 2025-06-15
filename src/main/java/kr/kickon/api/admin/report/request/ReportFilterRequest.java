package kr.kickon.api.admin.report.request;

import lombok.Data;

@Data
public class ReportFilterRequest {
    private String type; // "BOARD", "NEWS", "ALL"
    private String sort; // "REPORT_COUNT", "CREATED_AT"
    private Integer page = 0;
    private Integer size = 10;
}