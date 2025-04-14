package kr.kickon.api.global.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerService {
    @KafkaListener(topics = "game-updates", groupId = "gamble-group")
    public void listen(String message) {
        System.out.println("Kafka Received: " + message);
        // 메시지 처리 로직
    }
}