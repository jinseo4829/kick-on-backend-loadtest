package kr.kickon.api.global.common.annotations;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kr.kickon.api.global.common.ResponseDTO;
import org.springframework.http.MediaType;
// 안써도 됨
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Successfully processed the request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ResponseDTO.class)) // 기본값 설정
        )
})
public @interface ApiSuccessResponse {
}

