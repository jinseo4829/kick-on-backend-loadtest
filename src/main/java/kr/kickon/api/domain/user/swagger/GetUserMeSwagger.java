package kr.kickon.api.domain.user.swagger;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kr.kickon.api.domain.user.response.GetUserMeResponseDTO;
import org.springframework.http.MediaType;

@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Successfully processed the request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = GetUserMeResponseDTO.class))
        )
})
public @interface GetUserMeSwagger {
}
