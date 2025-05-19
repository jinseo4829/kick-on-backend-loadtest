package kr.kickon.api.admin.auth;

import kr.kickon.api.admin.auth.dto.LoginRequestDTO;
import kr.kickon.api.admin.auth.dto.LoginResponseDTO;
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

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        TokenDto token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED, LoginResponseDTO.builder()
                .refreshToken(token.getRefreshToken())
                .accessToken(token.getAccessToken())
                .build()
        ));
    }
}