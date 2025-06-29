package kr.kickon.api.global.auth;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.admin.root.AdminService;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.global.auth.jwt.admin.AdminJwtTokenProvider;
import kr.kickon.api.global.auth.jwt.dto.AiLoginRequest;
import kr.kickon.api.global.auth.jwt.dto.GetLoginResponse;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.auth.jwt.dto.TokenDto;
import kr.kickon.api.global.auth.jwt.dto.TokenRequestDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Admin;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "인증 관련")
@Slf4j
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AdminService adminService;
    private final AdminJwtTokenProvider adminJwtTokenProvider;

    @Operation(summary = "AI 유저 로그인", description = "AI 유저가 이메일과 패스워드로 로그인하여 무제한 AccessToken을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = GetLoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 이메일 또는 비밀번호)"),
            @ApiResponse(responseCode = "404", description = "AI 유저가 존재하지 않음")
    })
    @PostMapping("/ai/login")
    public ResponseEntity<ResponseDTO<TokenDto>> loginAsAiUser(@RequestBody AiLoginRequest request) {
        String email = request.getEmail();

        if (email == null || !email.contains("@") || !email.endsWith("kick-on.ai")) throw new UnauthorizedException(ResponseCode.INVALID_TOKEN, "유효하지 않은 AI 이메일입니다.");


        if (!"kickon2025!".equals(request.getPassword())) throw new UnauthorizedException(ResponseCode.INVALID_TOKEN, "비밀번호가 일치하지 않습니다.");


        Optional<User> user = userService.findUserByEmail(email);
        if (user.isEmpty()) throw new UnauthorizedException(ResponseCode.NOT_FOUND_USER, "해당 이메일의 AI 유저가 존재하지 않습니다.");


        // 기본 권한
        String authorities = "ROLE_GUEST, ROLE_OAUTH_FIRST_JOIN, ROLE_USER, ROLE_AI";

        Map<String, Object> claims = new HashMap<>();
        claims.put(jwtTokenProvider.AUTH_PK, user.get().getPk());
        claims.put(jwtTokenProvider.AUTHORITIES_KEY, authorities);

        // ✅ 유효기간이 사실상 무제한인 AccessToken 발급 (100년)
        String accessToken = jwtTokenProvider.createPermanentAccessToken(claims, user.get().getPk(), authorities);

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,
                new TokenDto(accessToken, null)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<TokenDto>> refreshToken(@RequestBody TokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException(ResponseCode.INVALID_REFRESH_TOKEN);
        }
        String authorities = "ROLE_GUEST, ROLE_OAUTH_FIRST_JOIN";
        Claims claims = jwtTokenProvider.getClaimsFromToken(refreshToken);
        User user = userService.findByPk(Long.parseLong(claims.get(jwtTokenProvider.AUTH_PK).toString()));
        if(user==null) throw new UnauthorizedException(ResponseCode.INVALID_REFRESH_TOKEN);
        long now = (new Date()).getTime();

        Map<String, Object> newClaims = new HashMap<>();
        if(user.getPrivacyAgreedAt()!=null) authorities += ", ROLE_USER";

        newClaims.put(jwtTokenProvider.AUTH_PK, user.getPk());
        newClaims.put(jwtTokenProvider.AUTHORITIES_KEY, authorities);
        String accessToken = jwtTokenProvider.createAccessToken(newClaims, user.getPk(), authorities);

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,
                new TokenDto(accessToken, refreshToken)));
    }

    @PostMapping("/admin/refresh")
    public ResponseEntity<ResponseDTO<TokenDto>> refreshAdminToken(@RequestBody TokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();
        if (!adminJwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException(ResponseCode.INVALID_REFRESH_TOKEN);
        }

        Claims claims = adminJwtTokenProvider.getClaimsFromToken(refreshToken);
        Admin admin = adminService.findByPk(Long.parseLong(claims.get(jwtTokenProvider.AUTH_PK).toString()));
        if (admin == null) throw new UnauthorizedException(ResponseCode.INVALID_REFRESH_TOKEN);

        String authorities = "ROLE_ADMIN";  // 관리자 권한만 부여

        Map<String, Object> newClaims = new HashMap<>();
        newClaims.put(adminJwtTokenProvider.AUTH_PK, admin.getPk());
        newClaims.put(adminJwtTokenProvider.AUTHORITIES_KEY, authorities);

        // 관리자용 access token 생성
        String accessToken = adminJwtTokenProvider.createAccessToken(newClaims, admin.getPk(), authorities);

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,
                new TokenDto(accessToken, refreshToken)));
    }
}
