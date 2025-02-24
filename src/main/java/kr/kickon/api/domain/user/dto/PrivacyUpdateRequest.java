package kr.kickon.api.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import kr.kickon.api.global.config.LocalDateTimeDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PrivacyUpdateRequest {
    @NotNull(message = "개인정보 동의 여부는 필수입니다.")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime privacyAgreedAt;

    @Null
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime marketingAgreedAt;
}