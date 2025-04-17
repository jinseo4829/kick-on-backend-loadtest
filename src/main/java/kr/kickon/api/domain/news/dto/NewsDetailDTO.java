package kr.kickon.api.domain.news.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.entities.News;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class NewsDetailDTO extends NewsListDTO {
    @Schema(example = "true", description = "킥 눌렀는지 여부")
    private Boolean isKicked;

    public NewsDetailDTO(News news, BaseUserDTO user, Integer likes, Integer views,Integer replies) {
        super(news, user, likes, views, replies);
    }
}
