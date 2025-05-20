package kr.kickon.api.global.auth.jwt.dto;

import kr.kickon.api.global.common.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;

// Authentication 객체 안에 사용자 정보를 담기 위한 클래스.
public class PrincipalUserDetail implements UserDetails, OAuth2User, Serializable {
    private final User user;
    private final Map<String, Object> attributes;
    private final Collection<GrantedAuthority>  authorities;

    // JWT 인증 시 사용
    public PrincipalUserDetail(User user, String roles) {
        this.user = user;
        this.authorities = createAuthorities(roles);
        this.attributes = Collections.emptyMap();  // jwt 인증에서는 사용 안해도 될 듯?
    }

    // OAuth2 인증 시 사용
    public PrincipalUserDetail(User user, Map<String, Object> attributes, String roles) {
        this.user = user;
        this.attributes = attributes;
        this.authorities = createAuthorities(roles);
    }

    @Override
    public String getName() {
        return user.getPk().toString();
    }



    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * 해당 유저의 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    private Collection<GrantedAuthority> createAuthorities(String roles){
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        for(String role : roles.split(",")){
            if (!StringUtils.hasText(role)) continue;
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return user.getPk().toString();
    }
}