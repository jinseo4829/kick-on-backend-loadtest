package kr.kickon.api.global.auth.oauth.dto;

import kr.kickon.api.global.common.enums.ProviderType;

public interface OAuth2UserInfo {
    String getProviderId();
    ProviderType getProvider();
    String getEmail();
    String getNickname();
    String getProfileImageUrl();
}