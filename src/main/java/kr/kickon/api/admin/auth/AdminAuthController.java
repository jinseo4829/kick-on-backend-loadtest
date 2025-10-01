package kr.kickon.api.admin.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import kr.kickon.api.admin.auth.dto.LoginRequestDTO;
import kr.kickon.api.admin.auth.dto.LoginResponseDTO;
import kr.kickon.api.admin.auth.response.PostLoginResponse;
import kr.kickon.api.domain.actualSeasonRanking.response.GetActualSeasonRankingResponse;
import kr.kickon.api.global.auth.jwt.admin.AdminJwtTokenProvider;
import kr.kickon.api.global.auth.jwt.dto.TokenDto;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminAuthService authService;
    private final AdminJwtTokenProvider adminJwtTokenProvider;

    @Operation(summary = "어드민 로그인", description = "이메일이랑 비밀번호 입력")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = PostLoginResponse.class))),
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        TokenDto token = authService.login(request.getEmail(), request.getPassword());
        // 쿠키에 토큰 설정
        adminJwtTokenProvider.setTokenCookies(response, token);

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED, LoginResponseDTO.builder()
                .refreshToken(token.getRefreshToken())
                .accessToken(token.getAccessToken())
                .build()
        ));
    }
}