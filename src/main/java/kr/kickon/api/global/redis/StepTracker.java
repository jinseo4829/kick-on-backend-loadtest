package kr.kickon.api.global.redis;

import kr.kickon.api.global.kafka.KafkaGameProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepTracker {
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaGameProducer kafkaProducer;

    private static final int TOTAL_STEPS = 4;

    /**
     * step 예시: step1, step2, ...
     */
    public void markStepComplete(String gameId, String step) {
        String completeKey = "game:step-complete:" + gameId;
        String logKey = "game:step-log:" + gameId;

        // 현재 step 로그 저장
        redisTemplate.opsForSet().add(logKey, step);
        redisTemplate.expire(logKey, Duration.ofHours(1));

        // 완료 step 수 증가
        Long completedSteps = redisTemplate.opsForValue().increment(completeKey);
        redisTemplate.expire(completeKey, Duration.ofHours(1));

        if (completedSteps != null && completedSteps >= TOTAL_STEPS) {
            kafkaProducer.sendStep5Trigger(gameId);
        }
    }

    public void clearStepTracker(String gameId) {
        redisTemplate.delete("game:step-complete:" + gameId);
        redisTemplate.delete("game:step-log:" + gameId);
    }
}