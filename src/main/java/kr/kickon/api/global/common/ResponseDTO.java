package kr.kickon.api.global.common;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Slf4j
@Schema(name = "ResponseDTO", description = "Response DTO for API responses")
public class ResponseDTO<T> {
    @Schema(example = "SUCCESS", description = "API CODE")
    private String code;
    @Schema(example = "성공", description = "API에 대한 메시지")
    private String message;
    @Schema(description = "응답 데이터, 객체일수도, 배열일수도 있음", nullable = true)
    private T data;
    @Schema(description = "메타 데이터",nullable = true)
    private Object meta;

    public ResponseDTO(String code, String message, T o, Object o1) {
        this.code = code;
        this.message = message;
        this.data = o;
        this.meta = o1;
    }

    public static <T> ResponseDTO<T> success(ResponseCode code) {
        return new ResponseDTO<>(code.getCode(), code.getMessage(), null, null);
    }

    // ✅ 성공 응답 (메타 없음)
    public static <T> ResponseDTO<T> success(ResponseCode code, T data) {
        return new ResponseDTO<>(code.getCode(), code.getMessage(), data, null);
    }

    // ✅ 성공 응답 (메타 포함)
    public static <T> ResponseDTO<T> success(ResponseCode code, T data, Object meta) {
        return new ResponseDTO<>(code.getCode(), code.getMessage(), data, meta);
    }

    public static <T> ResponseDTO<List<T>> success(ResponseCode code, List<T> dataList) {
        return new ResponseDTO<>(code.getCode(), code.getMessage(), dataList, null);
    }

    // ✅ 에러 응답
    public static ResponseDTO<Void> error(ResponseCode code) {
        return new ResponseDTO<>(code.getCode(), code.getMessage(), null, null);
    }

    public static <T> ResponseDTO<T> error(ResponseCode code, T data) {
        return new ResponseDTO<>(code.getCode(), code.getMessage(), data, null);
    }


    public static ResponseDTO<Void> error(String code, String message) {
        return new ResponseDTO<>(code, message, null, null);
    }
}
