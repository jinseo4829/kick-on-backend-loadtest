package kr.kickon.api.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class FavoriteTeamDTO extends TeamDTO{
    @Schema(description = "우선순위", example = "3")
    private Integer priorityNum;
}
