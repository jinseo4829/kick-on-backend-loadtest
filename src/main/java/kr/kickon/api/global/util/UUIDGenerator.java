package kr.kickon.api.global.util;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Component
public class UUIDGenerator {
    public <T> String generateUniqueUUID(Function<String, Optional<T>> findById) {
        String uuid;
        do {
            uuid = UUID.randomUUID().toString();
        } while (findById.apply(uuid).isPresent());  // UUID 중복 체크
        return uuid;
    }
}
