package kr.kickon.api.global.kafka;

import kr.kickon.api.global.redis.StepTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class Step5RankingUpdater {
    private StepTracker stepTracker;

    @KafkaListener(
            topics = "step5-ready",
            groupId = "step5-ranking-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStep5(String message, @Header(KafkaHeaders.RECEIVED_KEY) String gameId,
                            Acknowledgment ack) {
        try {


            updateFinalTeamRanking(gameId); // step 5 수행

            stepTracker.clearStepTracker(gameId);
            stepTracker.markStepComplete(gameId,"step5");
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Step 5 error for gameId {}: {}", gameId, e.getMessage());
        }
    }

    private void updateFinalTeamRanking(String gameId) {
        // TODO: step5 구현 함수 호출
    }
}