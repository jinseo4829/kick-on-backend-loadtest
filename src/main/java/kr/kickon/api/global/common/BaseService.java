package kr.kickon.api.global.common;

import java.util.Optional;

public interface BaseService<T> {
    public Optional<T> findById(String uuid);
}
