package kr.kickon.api.global.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaGameProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendGameResultProcessing(String gameId) {
        kafkaTemplate.send("game-result-processing", gameId, "START");
    }

    public void sendStep5Trigger(String gameId) {
        kafkaTemplate.send("step5-ready", gameId, "STEP5_READY");
    }
}