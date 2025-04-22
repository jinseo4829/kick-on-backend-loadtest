package kr.kickon.api.domain.news.dto;

import kr.kickon.api.global.common.PagedMetaDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class PaginatedNewsListDTO extends PagedMetaDTO {
    List<NewsListDTO> newsList;
    public PaginatedNewsListDTO(Integer currentPage, Integer pageSize, Long totalItems, List<NewsListDTO> news) {
        super(currentPage, pageSize, totalItems);
        this.newsList = news;
    }

    public PaginatedNewsListDTO(List<NewsListDTO> news, Boolean hasNext){
        super(hasNext);
        this.newsList = news;
    }
}
