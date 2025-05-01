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
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    @Value("${spring.config.activate.on-profile}")
    private String env;
    @Value("${spring.kafka.topic.game-result}")
    public String GAME_RESULT_TOPIC;
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    // ✅ Producer 설정
    @Bean
    public ProducerFactory<String, ApiGamesDTO> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ApiGamesDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ✅ Listener Container 설정
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ApiGamesDTO> kafkaListenerContainerFactory(
            ConsumerFactory<String, ApiGamesDTO> consumerFactory,
            KafkaTemplate<String, ApiGamesDTO> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, ApiGamesDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);  // 파티션 수에 따라 설정
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate)); // errorHandler에 kafkaTemplate 주입

        return factory;
    }

    // ✅ 에러 핸들러(DLT 포함)
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, ApiGamesDTO> kafkaTemplate) {
        // DLQ 설정
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));

        // 재시도 정책: 실패하면 3번 재시도, 5초 간격
        FixedBackOff fixedBackOff = new FixedBackOff(5000L, 3);

        return new DefaultErrorHandler(recoverer, fixedBackOff);
    }

    // ✅ 원래 토픽 생성
    @Bean
    public NewTopic gamesTopic() {
        return TopicBuilder.name(GAME_RESULT_TOPIC)
                .partitions(3)
                .replicas(env.equals("prod") ? 2 : 1)
                .build();
    }

    // ✅ DLT 토픽 생성
    @Bean
    public NewTopic gamesTopicDlt() {
        return TopicBuilder.name(GAME_RESULT_TOPIC + ".DLT")
                .partitions(3)
                .replicas(env.equals("prod") ? 2 : 1)
                .build();
    }
}

