package kr.kickon.api.global.auth;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.auth.jwt.dto.TokenDto;
import kr.kickon.api.global.auth.jwt.dto.TokenRequestDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "인증 관련")
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<TokenDto>> refreshToken(@RequestBody TokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException(ResponseCode.INVALID_REFRESH_TOKEN);
        }
        String authorities = "GUEST, ROLE_OAUTH_FIRST_JOIN";
        Claims claims = jwtTokenProvider.getClaimsFromToken(refreshToken);
        User user = userService.findByPk(Long.parseLong(claims.get(jwtTokenProvider.AUTH_PK).toString()));
        if(user==null) throw new UnauthorizedException(ResponseCode.INVALID_REFRESH_TOKEN);
        long now = (new Date()).getTime();

        Map<String, Object> newClaims = new HashMap<>();
        if(user.getPrivacyAgreedAt()!=null) authorities += ", USER";

        newClaims.put(jwtTokenProvider.AUTH_PK, user.getPk());
        newClaims.put(jwtTokenProvider.AUTHORITIES_KEY, authorities);
        refreshToken = jwtTokenProvider.createRefreshToken(claims, user.getPk());
        String accessToken = jwtTokenProvider.createAccessToken(claims, user.getPk(), authorities);

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,
                new TokenDto(accessToken, refreshToken)));
    }
}
