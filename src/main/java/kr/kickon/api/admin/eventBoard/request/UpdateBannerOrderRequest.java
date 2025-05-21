package kr.kickon.api.admin.eventBoard.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "배너 순서 업데이트를 위한 request")
public class UpdateBannerOrderRequest {
    @Schema(description = "Banner 리스트")
    private List<BannerOrder> orders;

    @Getter
    @Setter
    @Schema(description = "배너 DTO")
    public static class BannerOrder {
        @Min(value = 1, message = "pk는 1 이상의 값이어야 합니다.")
        @Schema(description = "배너 pk", example = "1")
        private Long pk;

        @Min(value = 1, message = "순서는 1 이상의 값이어야 합니다.")
        @Schema(description = "순서", example = "1")
        private Integer orderNum;
    }
}