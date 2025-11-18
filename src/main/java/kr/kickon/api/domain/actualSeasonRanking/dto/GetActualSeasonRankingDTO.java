package kr.kickon.api.domain.actualSeasonRanking.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import kr.kickon.api.global.common.ExampleConstants;

@Getter
@Setter
public class GetActualSeasonRankingDTO {
    @Schema(example = "1", description = "순위")
    private Integer rankOrder;

    @Schema(example = ExampleConstants.teamLogoUrl, description = "팀 로고 url")
    private String teamLogoUrl;

    @Schema(example = ExampleConstants.teamName, description = "팀 이름")
    private String teamName;

    @Schema(example = "1", description = "게임 수")
    private Integer gameNum;

    @Schema(example = "12", description = "승점")
    private Integer points;

    @Schema(example = "10", description = "득점 수")
    private Integer wonScores;

    public GetActualSeasonRankingDTO(Integer rankOrder, String teamLogoUrl, String teamName, Integer gameNum, Integer points, Integer wonScores) {
        this.rankOrder = rankOrder;
        this.teamLogoUrl = teamLogoUrl;
        this.teamName = teamName;
        this.gameNum = gameNum;
        this.points = points;
        this.wonScores = wonScores;
    }
}
