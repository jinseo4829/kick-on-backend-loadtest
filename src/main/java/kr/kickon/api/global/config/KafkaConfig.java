package kr.kickon.api.global.config;

import kr.kickon.api.domain.migration.dto.ApiGamesDTO;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    private final KafkaTemplate<String, ApiGamesDTO> kafkaTemplate;
    @Value("${spring.config.activate.on-profile}")
    private String env;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3); // 파티션 수에 따라 설정
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));
        return factory;
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, ApiGamesDTO> kafkaTemplate) {
        // DLQ 설정
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));

        // 재시도 정책: 실패하면 3번 재시도, 5초 간격
        FixedBackOff fixedBackOff = new FixedBackOff(5000L, 3);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, fixedBackOff);

        return errorHandler;
    }
    // ✅ 원래 토픽 생성
    @Bean
    public NewTopic gamesTopic() {
        return TopicBuilder.name("games-topic")
                .partitions(3)
                .replicas(env.equals("prod") ? 2 : 1)
                .build();
    }

    // ✅ DLT 토픽 생성
    @Bean
    public NewTopic gamesTopicDlt() {
        return TopicBuilder.name("games-topic.DLT")
                .partitions(3)
                .replicas(env.equals("prod") ? 2 : 1)
                .build();
    }
}

