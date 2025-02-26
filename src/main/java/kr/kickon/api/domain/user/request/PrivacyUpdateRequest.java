package kr.kickon.api.domain.user.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(example = "2025-02-24T17:00:00Z", description = "The date and time when the user agreed to the privacy policy.")
    private LocalDateTime privacyAgreedAt;

    @Null
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(example = "2025-02-24T17:00:00Z", description = "The date and time when the user agreed to the marketing policy.")
    private LocalDateTime marketingAgreedAt;
}