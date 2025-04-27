package kr.kickon.api.global.kafka;

import kr.kickon.api.global.redis.StepTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.KafkaHeaders;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameResultProcessor {
    private final KafkaGameProducer kafkaProducer;
    private final StepTracker stepTracker;

    @KafkaListener(
            topics = "game-result-processing",
            groupId = "result-processing-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleGameResult(String message, @Header(KafkaHeaders.RECEIVED_KEY) String gameId,
                                 Acknowledgment ack) {
        try {
            log.info("[GameId: {}] Processing game result", gameId);

            // Step 1
            processUserPredictions(gameId);

            // Step 2
            saveGameResult(gameId);

            // Step 3
            updateTeamAvgPoints(gameId);

            // Step 4
            updateTeamRankingStats(gameId);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing gameId {}: {}", gameId, e.getMessage());
        }
    }

    private void processUserPredictions(String gameId) {
        // TODO: step1 구현 함수 호출
        stepTracker.markStepComplete(gameId,"step1");
    }

    private void saveGameResult(String gameId) {
        // TODO: step2 구현 함수 호출
        stepTracker.markStepComplete(gameId,"step2");
    }

    private void updateTeamAvgPoints(String gameId) {
        // TODO: step3 구현 함수 호출
        stepTracker.markStepComplete(gameId,"step3");
    }

    private void updateTeamRankingStats(String gameId) {
        // TODO: step4 구현 함수 호출
        stepTracker.markStepComplete(gameId,"step4");
    }
}