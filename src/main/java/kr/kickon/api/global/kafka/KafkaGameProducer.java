package kr.kickon.api.global.kafka;

import kr.kickon.api.domain.migration.dto.ApiGamesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KafkaGameProducer {
    private final KafkaTemplate<String, ApiGamesDTO> kafkaTemplate;

    @Value("${spring.kafka.topic.game-result}")
    public String GAME_RESULT_TOPIC;

    public CompletableFuture<SendResult<String, ApiGamesDTO>> sendGameResultProcessing(String gameId, ApiGamesDTO gameData) {
        System.out.println(gameId + gameData);
        CompletableFuture<SendResult<String, ApiGamesDTO>> future = kafkaTemplate.send(GAME_RESULT_TOPIC, gameId, gameData);

        future.thenAccept(result -> {
            System.out.println("✅ Kafka Send Success: " + result.getRecordMetadata());
        }).exceptionally(ex -> {
            System.err.println("❌ Kafka Send Failed: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });

        return future;
    }
}