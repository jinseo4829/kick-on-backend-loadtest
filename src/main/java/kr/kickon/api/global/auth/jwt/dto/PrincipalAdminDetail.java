package kr.kickon.api.global.auth.jwt.dto;

import kr.kickon.api.global.common.entities.Admin;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;

public class PrincipalAdminDetail implements UserDetails, Serializable {

    private final Admin admin;
    private final Map<String, Object> attributes;
    private final Collection<GrantedAuthority> authorities;

    // JWT 인증 시 사용
    public PrincipalAdminDetail(Admin admin, String roles) {
        this.admin = admin;
        this.authorities = createAuthorities(roles);
        this.attributes = Collections.emptyMap();  // JWT 인증에서는 사용 안 해도 될 듯
    }
    public PrincipalAdminDetail(Admin admin, List<GrantedAuthority> authorities) {
        this.admin = admin;
        this.authorities = authorities;
        this.attributes = Collections.emptyMap();
    }

    // OAuth2 인증 시 사용
    public PrincipalAdminDetail(Admin admin, Map<String, Object> attributes, String roles) {
        this.admin = admin;
        this.attributes = attributes;
        this.authorities = createAuthorities(roles);
    }

    private Collection<GrantedAuthority> createAuthorities(String roles){
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        for(String role : roles.split(",")){
            if (!StringUtils.hasText(role)) continue;
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }


    public String getPk() {
        return admin.getPk().toString();
    }

    /**
     * 해당 유저의 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return admin.getPassword(); // 암호화된 비밀번호 반환
    }

    @Override
    public String getUsername() {
        return admin.getEmail(); // 로그인 시 사용할 email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (true로 설정)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 (true로 설정)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 여부 (true로 설정)
    }

    @Override
    public boolean isEnabled() {
        return admin.getStatus().equals("ACTIVATED"); // 계정 활성화 여부
    }
}