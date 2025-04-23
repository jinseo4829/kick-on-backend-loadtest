package kr.kickon.api.domain.newsReply.dto;

import kr.kickon.api.global.common.PagedMetaDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class PaginatedNewsReplyListDTO extends PagedMetaDTO {
    List<ReplyDTO> replyList;

    public PaginatedNewsReplyListDTO(Integer currentPage, Integer pageSize, Long totalItems, List<ReplyDTO> replyList) {
        super(currentPage, pageSize, totalItems);
        this.replyList = replyList;
    }

    public PaginatedNewsReplyListDTO(List<ReplyDTO> replyList, Boolean hasNext){
        super(hasNext);
        this.replyList = replyList;
    }
}
