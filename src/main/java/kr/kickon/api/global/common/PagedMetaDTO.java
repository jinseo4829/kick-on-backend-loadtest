package kr.kickon.api.global.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
@AllArgsConstructor
public class PagedMetaDTO {
    private final Integer currentPage; // 현재 페이지
    private final Integer pageSize;    // 페이지 크기
    private final Long totalItems; // 전체 항목 수
    private final Integer totalPages;  // 전체 페이지 수

    public PagedMetaDTO(Integer currentPage, Integer pageSize, Long totalItems) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }
}