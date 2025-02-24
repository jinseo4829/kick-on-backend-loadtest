package kr.kickon.api.global.auth.oauth.dto;

import kr.kickon.api.global.common.enums.ProviderType;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes;
    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("id");
    }

    @Override
    public ProviderType getProvider() {
        return ProviderType.NAVER;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {
        return (String) attributes.get("nickname");
    }

    @Override
    public String getProfileImageUrl(){
        return (String) attributes.get("profile_image");
    }
}