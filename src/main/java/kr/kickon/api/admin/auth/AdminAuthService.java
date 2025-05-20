package kr.kickon.api.admin.auth;

import kr.kickon.api.admin.root.AdminService;
import kr.kickon.api.global.auth.jwt.admin.AdminJwtTokenProvider;
import kr.kickon.api.global.auth.jwt.dto.PrincipalAdminDetail;
import kr.kickon.api.global.auth.jwt.dto.TokenDto;
import kr.kickon.api.global.common.entities.Admin;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final AdminService adminService;
    private final AdminJwtTokenProvider adminJwtTokenProvider;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public TokenDto login(String email, String password) {
        // 1. 이메일로 관리자 조회
        Admin admin = adminService.findByEmail(email);
        if (admin == null) {
            throw new UnauthorizedException(ResponseCode.NOT_FOUND_ADMIN);
        }

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new UnauthorizedException(ResponseCode.NOT_FOUND_ADMIN);
        }

        // 3. 인증 객체 생성
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        PrincipalAdminDetail principal = new PrincipalAdminDetail(admin, authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        // 4. 토큰 생성
        return adminJwtTokenProvider.createToken(authentication);
    }
}
