package kr.kickon.api.domain.news.dto;

import kr.kickon.api.global.common.PagedMetaDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class InfiniteScrollNewsListDTO extends PagedMetaDTO {
    private List<NewsListDTO> newsList;


    public InfiniteScrollNewsListDTO(List<NewsListDTO> news, Boolean hasNext) {
        super(hasNext);
        newsList = news;
    }
}