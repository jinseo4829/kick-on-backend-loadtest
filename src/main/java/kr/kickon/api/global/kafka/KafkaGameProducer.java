package kr.kickon.api.global.kafka;

import kr.kickon.api.domain.migration.dto.ApiGamesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaGameProducer {
    private final KafkaTemplate<String, ApiGamesDTO> kafkaTemplate;

    public void sendGameResultProcessing(String gameId, ApiGamesDTO gameData) {
        kafkaTemplate.send("game-result-processing", gameId, gameData);
    }

    public void sendStep5Trigger(String gameId, ApiGamesDTO gameData) {
        kafkaTemplate.send("step5-ready", gameId, gameData);
    }
}