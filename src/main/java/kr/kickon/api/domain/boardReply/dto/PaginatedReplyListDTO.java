package kr.kickon.api.domain.boardReply.dto;

import kr.kickon.api.global.common.PagedMetaDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PaginatedReplyListDTO extends PagedMetaDTO {
    List<ReplyDTO> replyList;

    public PaginatedReplyListDTO(Integer currentPage, Integer pageSize, Long totalItems, List<ReplyDTO> replyList) {
        super(currentPage, pageSize, totalItems);
        this.replyList = replyList;
    }
    public PaginatedReplyListDTO(List<ReplyDTO> replyList, Boolean hasNext){
        super(hasNext);
        this.replyList = replyList;
    }
}
