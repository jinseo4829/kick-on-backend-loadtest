package kr.kickon.api.domain.userPointEvent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "팀 내 유저 랭킹 DTO")
public class UserRankingDTO {
    @Schema(example = ExampleConstants.id, description = "유저 ID 값")
    private String userId;

    @Schema(example = "1123", description = "포인트 총합")
    private Integer totalPoints;

    @Schema(example = "1", description = "랭킹")
    private Integer ranking;
}
