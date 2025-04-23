package kr.kickon.api.domain.board.dto;

import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.global.common.PagedMetaDTO;
import lombok.Getter;
import java.util.List;

@Getter
public class PaginatedBoardListDTO extends PagedMetaDTO {
    List<BoardListDTO> boardList;
    public PaginatedBoardListDTO(Integer currentPage, Integer pageSize, Long totalItems, List<BoardListDTO> boards) {
        super(currentPage, pageSize, totalItems);
        this.boardList = boards;
    }

    public PaginatedBoardListDTO(List<BoardListDTO> board, Boolean hasNext){
        super(hasNext);
        this.boardList = board;
    }
}
