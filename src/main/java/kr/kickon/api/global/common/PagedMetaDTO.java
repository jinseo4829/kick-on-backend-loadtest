package kr.kickon.api.global.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
public class PagedMetaDTO {
    private Integer currentPage; // 현재 페이지
    private Integer pageSize;    // 페이지 크기
    private Long totalItems; // 전체 항목 수
    private Integer totalPages;  // 전체 페이지 수
    private Boolean hasNext;

    public PagedMetaDTO(Integer currentPage, Integer pageSize, Long totalItems) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.hasNext = null;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public PagedMetaDTO(boolean hasNext) {
        this.hasNext = hasNext;
    }
}