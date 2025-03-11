package kr.kickon.api.domain.gambleSeasonRanking.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetGambleSeasonRankingDTO {
    @Schema(example = "1", description = "순위")
    private Integer rankOrder;

    @Schema(example = ExampleConstants.teamLogoUrl, description = "팀 로고 url")
    private String teamLogoUrl;

    @Schema(example = ExampleConstants.teamName, description = "팀 이름")
    private String teamName;

    @Schema(example = "1", description = "게임 수")
    private Integer gameNum;

    @Schema(example = "12", description = "점수")
    private Float points;

    public GetGambleSeasonRankingDTO(Integer rankOrder, String teamLogoUrl, String teamName, Integer gameNum, Float points) {
        this.rankOrder = rankOrder;
        this.teamLogoUrl = teamLogoUrl;
        this.teamName = teamName;
        this.gameNum = gameNum;
        this.points = points;
    }
}
