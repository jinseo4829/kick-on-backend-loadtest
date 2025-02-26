package kr.kickon.api.global.common;

import java.util.Optional;

public interface BaseService<T> {
    public T findById(String uuid);
    public T findByPk(Long pk);
}
