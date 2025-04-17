package kr.kickon.api.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.userGameGamble.dto.UserGameGambleDTO;
import kr.kickon.api.global.common.entities.Game;
import kr.kickon.api.global.common.enums.GameStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "게임 아이템 DTO")
public class GameDTO {
    private TeamDTO homeTeam;

    private TeamDTO awayTeam;

    private GambleResultDTO gambleResult;

    private UserGameGambleDTO myGambleResult;

    @Schema(description = "게임 pk", example = "1")
    private Long pk;

    @Schema(description = "홈 스코어", example = "1")
    private Integer homeScore;

    @Schema(description = "어웨이 스코어", example = "1")
    private Integer awayScore;

    @Schema(description = "라운드", example = "Regular Season - 1")
    private String round;

    @Schema(description = "홈 승부차기 스코어", example = "null")
    private Integer homePenaltyScore = null;

    @Schema(description = "어웨이 승부차기 스코어", example = "null")
    private Integer awayPenaltyScore = null;

    @Schema(description = "경기 상태값", example = "PENDING")
    private GameStatus gameStatus;

    @Schema(description = "경기 시작 일시", example = "2021-12-12 12:00:22")
    private LocalDateTime startAt;

    public GameDTO(TeamDTO homeTeam, TeamDTO awayTeam, Game game, GambleResultDTO gambleResult) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.gambleResult = gambleResult;
        pk = game.getPk();
        homeScore = game.getHomeScore();
        awayScore = game.getAwayScore();
        round = game.getRound();
        homePenaltyScore = game.getHomePenaltyScore();
        awayPenaltyScore = game.getAwayPenaltyScore();
        gameStatus = game.getGameStatus();
        startAt = game.getStartedAt();
    }
}

