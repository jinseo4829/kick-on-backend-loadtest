package kr.kickon.api.global.kafka;

import jakarta.transaction.Transactional;
import kr.kickon.api.domain.gambleSeason.GambleSeasonService;
import kr.kickon.api.domain.gambleSeasonPoint.GambleSeasonPointService;
import kr.kickon.api.domain.gambleSeasonRanking.GambleSeasonRankingService;
import kr.kickon.api.domain.gambleSeasonTeam.GambleSeasonTeamService;
import kr.kickon.api.domain.game.GameService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.migration.dto.ApiGamesDTO;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.userGameGamble.UserGameGambleService;
import kr.kickon.api.domain.userPointDetail.UserPointDetailService;
import kr.kickon.api.domain.userPointEvent.UserPointEventService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.*;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.KafkaHeaders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static kr.kickon.api.domain.game.GameService.getGameStatus;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameResultProcessor {
    private final KafkaGameProducer kafkaProducer;
    private final GameService gameService;
    private final UserGameGambleService userGameGambleService;
    private final TeamService teamService;
    private final UUIDGenerator uuidGenerator;
    private final UserPointDetailService userPointDetailService;
    private final UserPointEventService userPointEventService;
    private final GambleSeasonPointService gambleSeasonPointService;
    private final GambleSeasonRankingService gambleSeasonRankingService;
    private final GambleSeasonTeamService gambleSeasonTeamService;

    private static List<String> scheduledStatus = new ArrayList<>(Arrays.asList(GameService.ScheduledStatus));
    private static List<String> finishedStatus = new ArrayList<>(Arrays.asList(GameService.FinishedStatus));
    private static GameStatus gameStatus;

    @Transactional
    @KafkaListener(
            topics = "${spring.kafka.topic.game-result}",
            groupId = "result-processing-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleGameResult(@Header(KafkaHeaders.RECEIVED_KEY) String gameId,
                                 ApiGamesDTO gameData,
                                 Acknowledgment ack) {
        try {
            log.info("[GameId: {}] Processing game result", gameId);

            // Step 1
            Game game = saveGameResult(gameData);
            gameStatus = getGameStatus(gameData, scheduledStatus, finishedStatus);
            // step 2
            processUserPredictions(game, gameData, gameStatus);
            // Step 3
            updateTeamAvgPoints(game,gameData);
            // Step 4
            updateTeamRankingStats(game);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing gameId {}: {}", gameId, e.getMessage());
            throw e; // 반드시 다시 던져야 Kafka가 "얘 실패했네" 인식함
        }
    }
    private Game saveGameResult(ApiGamesDTO apiData) {
        // TODO: step1 구현 함수 호출
        Game game = null;

        try{
            // 필수 값 체크
            game = gameService.findByApiId(apiData.getId());
            game.setGameStatus(gameStatus);
            game.setAwayPenaltyScore(apiData.getAwayPenaltyScore());
            game.setHomePenaltyScore(apiData.getHomePenaltyScore());
            game.setHomeScore(apiData.getHomeScore());
            game.setAwayScore(apiData.getAwayScore());
        }catch (NotFoundException ignore){
            Team homeTeam, awayTeam;
            homeTeam = teamService.findByApiId(apiData.getHomeTeamId());
            awayTeam = teamService.findByApiId(apiData.getAwayTeamId());
            if(homeTeam == null || awayTeam==null){}
            else{
                String id = uuidGenerator.generateUniqueUUID(gameService::findById);

                game = Game.builder()
                        .id(id)
                        .gameStatus(gameStatus)
                        .awayPenaltyScore(apiData.getAwayPenaltyScore())
                        .homePenaltyScore(apiData.getHomePenaltyScore())
                        .actualSeason(apiData.getActualSeason())
                        .apiId(apiData.getId())
                        .homeScore(apiData.getHomeScore())
                        .awayScore(apiData.getAwayScore())
                        .round(apiData.getRound())
                        .homeTeam(homeTeam)
                        .awayTeam(awayTeam)
                        .startedAt(apiData.getDate())
                        .build();
            }
        }
        gameService.save(game);
        return game;
    }

    private void processUserPredictions(Game game, ApiGamesDTO apiGamesDTO, GameStatus gameStatus) {
        // 유저 Gamble Status 업데이트 하는거임
        List<UserGameGamble> userGameGambles = userGameGambleService.findByGameApiId(game.getApiId());
        List<String> userPointEventIds = new ArrayList<>();
        List<String> userPointDetailIds = new ArrayList<>();
        for(UserGameGamble userGameGamble : userGameGambles) {
            // 경기 결과가 확정되지 않은 경우, 상태를 변경하지 않음
            if (gameStatus == GameStatus.PENDING || gameStatus == GameStatus.PROCEEDING ||
                    gameStatus == GameStatus.CANCELED || gameStatus == GameStatus.POSTPONED) {
                continue;
            }

            PredictedResult predictedResult = userGameGamble.getPredictedResult();
            GambleStatus gambleStatus;

            // 예측 결과와 실제 경기 결과 비교
            if ((predictedResult == PredictedResult.HOME && gameStatus == GameStatus.HOME) ||
                    (predictedResult == PredictedResult.AWAY && gameStatus == GameStatus.AWAY) ||
                    (predictedResult == PredictedResult.DRAW && gameStatus == GameStatus.DRAW)) {

                // 정확한 점수까지 예측했는지 확인
                if (userGameGamble.getPredictedHomeScore().equals(apiGamesDTO.getHomeScore()) &&
                        userGameGamble.getPredictedAwayScore().equals(apiGamesDTO.getAwayScore())) {
                    gambleStatus = GambleStatus.PERFECT; // 정확한 점수까지 맞춘 경우
                } else {
                    gambleStatus = GambleStatus.SUCCEED; // 승부 예측만 맞춘 경우
                }
            } else {
                gambleStatus = GambleStatus.FAILED; // 예측 실패
            }

            // 상태 업데이트
            userGameGamble.setGambleStatus(gambleStatus);
            // 상태에 따라 포인트를 적립
            int points = 0;
            if (userGameGamble.getGambleStatus() == GambleStatus.SUCCEED) {
                points = 1;
            } else if (userGameGamble.getGambleStatus() == GambleStatus.PERFECT) {
                points = 3;
            }

            if (points > 0) {
                // 포인트를 UserPointDetail에 추가
                String userPointEventId = "";
                String userPointDetailId = "";
                do {
                    userPointDetailId = uuidGenerator.generateUniqueUUID(userPointDetailService::findById);
                    // 이미 생성된 ID가 배열에 있는지 확인
                } while (userPointDetailIds.contains(userPointDetailId)); // 중복이 있을 경우 다시 생성
                do {
                    userPointEventId = uuidGenerator.generateUniqueUUID(userPointDetailService::findById);
                    // 이미 생성된 ID가 배열에 있는지 확인
                } while (userPointEventIds.contains(userPointEventId)); // 중복이 있을 경우 다시 생성
                userPointDetailIds.add(userPointDetailId);
                userPointEventIds.add(userPointEventId);
                UserPointEvent userPointEvent = UserPointEvent.builder()
                        .id(userPointEventId)
                        .point(points)
                        .pointStatus(PointStatus.SAVE)
                        .user(userGameGamble.getUser())
                        .category(PointCategory.GAMBLE)
                        .build();
                UserPointDetail userPointDetail = UserPointDetail.builder()
                        .id(userPointDetailId)
                        .pointStatus(PointStatus.SAVE)
                        .user(userGameGamble.getUser())
                        .point(points)
                        .userPointEvent(userPointEventService.save(userPointEvent))
                        .build();
                userPointDetailService.save(userPointDetail);
            }
        }
        userGameGambleService.saveAll(userGameGambles);
    }

    private void updateTeamAvgPoints(Game game, ApiGamesDTO apiGamesDTO) {
        // TODO: step3 구현 함수 호출
        List<String> homeGambleSeasonPointIds = new ArrayList<>();
        List<String> awayGambleSeasonPointIds = new ArrayList<>();
        List<UserGameGamble> userGameGambles = userGameGambleService.findByGameApiId(game.getApiId());
        // 홈팀과 어웨이팀으로 구분
        List<UserGameGamble> homeTeamGambles = userGameGambles.stream()
                .filter(g -> g.getSupportingTeam().getApiId().equals(apiGamesDTO.getHomeTeamId()))
                .toList();

        List<UserGameGamble> awayTeamGambles = userGameGambles.stream()
                .filter(g -> g.getSupportingTeam().getApiId().equals(apiGamesDTO.getAwayTeamId()))
                .toList();
        String homeGambleSeasonPointId = "";
        String awayGambleSeasonPointId = "";
        do {
            homeGambleSeasonPointId = uuidGenerator.generateUniqueUUID(gambleSeasonPointService::findById);
            // 이미 생성된 ID가 배열에 있는지 확인
        } while (homeGambleSeasonPointIds.contains(homeGambleSeasonPointId)); // 중복이 있을 경우 다시 생성
        do {
            awayGambleSeasonPointId = uuidGenerator.generateUniqueUUID(gambleSeasonPointService::findById);
            // 이미 생성된 ID가 배열에 있는지 확인
        } while (awayGambleSeasonPointIds.contains(awayGambleSeasonPointId)); // 중복이 있을 경우 다시 생성
        homeGambleSeasonPointIds.add(homeGambleSeasonPointId);
        awayGambleSeasonPointIds.add(awayGambleSeasonPointId);

        // 홈팀 평균 포인트 저장
        double homeAvgPoints = calculateAveragePoints(homeTeamGambles);

        GambleSeasonTeam homeGambleSeasonTeam = gambleSeasonTeamService.findRecentOperatingByTeamPk(game.getHomeTeam().getPk());
//            System.out.println(homeGambleSeasonTeam.getTeam());
        if(homeGambleSeasonTeam != null){
            Team homeTeam = teamService.findByApiId(apiGamesDTO.getHomeTeamId());
            Team awayTeam = teamService.findByApiId(apiGamesDTO.getAwayTeamId());
            GambleSeasonPoint homeSeasonPoint = GambleSeasonPoint.builder()
                    .id(homeGambleSeasonPointId)
                    .gambleSeason(homeGambleSeasonTeam.getGambleSeason())
                    .averagePoints((int) Math.round(homeAvgPoints * 1000))
                    .team(homeTeam)
                    .game(game)
                    .build();
            gambleSeasonPointService.save(homeSeasonPoint);

            // 어웨이팀 평균 포인트 저장
            double awayAvgPoints = calculateAveragePoints(awayTeamGambles);
            GambleSeasonPoint awaySeasonPoint = GambleSeasonPoint.builder()
                    .id(awayGambleSeasonPointId)
                    .gambleSeason(homeGambleSeasonTeam.getGambleSeason())
                    .averagePoints((int) Math.round(awayAvgPoints * 1000))
                    .team(awayTeam)
                    .game(game)
                    .build();
            gambleSeasonPointService.save(awaySeasonPoint);
            // 랭킹 업데이트
            GambleSeasonRanking homeGambleSeasonRanking = gambleSeasonRankingService.findByTeamPk(homeSeasonPoint.getTeam().getPk());
            homeGambleSeasonRanking.setPoints(homeGambleSeasonRanking.getPoints() + homeSeasonPoint.getAveragePoints());
            GambleSeasonRanking awayGambleSeasonRanking = gambleSeasonRankingService.findByTeamPk(awaySeasonPoint.getTeam().getPk());
            awayGambleSeasonRanking.setPoints(awayGambleSeasonRanking.getPoints() + awaySeasonPoint.getAveragePoints());
            gambleSeasonRankingService.save(homeGambleSeasonRanking);
            gambleSeasonRankingService.save(awayGambleSeasonRanking);
        }
    }

    private void updateTeamRankingStats(Game game) {
        gambleSeasonRankingService.updateGameNumOnlyByActualSeason(game.getActualSeason().getPk());
    }

    // 평균 포인트 계산 메서드
    double calculateAveragePoints(List<UserGameGamble> gambles) {
        return gambles.stream()
                .filter(g -> g.getGambleStatus() == GambleStatus.SUCCEED || g.getGambleStatus() == GambleStatus.PERFECT)
                .mapToInt(g -> g.getGambleStatus() == GambleStatus.PERFECT ? 3 : 1)
                .average()
                .orElse(0.0);
    }
}