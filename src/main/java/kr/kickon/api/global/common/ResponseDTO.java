package kr.kickon.api.global.common;

import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResponseDTO<T> {
    private final String code;
    private final String message;
    private final T data;
    private final Object meta;


    public static <T> ResponseDTO<T> success(ResponseCode code) {
        return new ResponseDTO<>(code.getCode(), code.getMessage(),null,null);
    }

    // ✅ 성공 응답 (메타 없음)
    public static <T> ResponseDTO<T> success(ResponseCode code, T data) {
        return new ResponseDTO<>(code.getCode(), code.getMessage(), data, null);
    }

    // ✅ 성공 응답 (메타 포함)
    public static <T> ResponseDTO<T> success(ResponseCode code, T data, Object meta) {
        return new ResponseDTO<>(code.getCode(), code.getMessage(), data, meta);
    }

    // ✅ 에러 응답
    public static ResponseDTO<Void> error(ResponseCode code) {
        return new ResponseDTO<>(code.getCode(), code.getMessage(), null, null);
    }

    public static <T> ResponseDTO<T> error(ResponseCode code, T data) {
        return new ResponseDTO<>(code.getCode(), code.getMessage(), data, null);
    }
}
