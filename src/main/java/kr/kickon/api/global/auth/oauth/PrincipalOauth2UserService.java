package kr.kickon.api.global.auth.oauth;

import jakarta.transaction.Transactional;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.auth.jwt.TokenDto;
import kr.kickon.api.global.auth.kakao.KakaoUserInfo;
import kr.kickon.api.global.auth.naver.NaverUserInfo;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ProviderType;
import kr.kickon.api.global.common.enums.Role;
import kr.kickon.api.global.common.enums.UserAccountStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = null;

        log.info(oAuth2User.getAttributes().toString());

        if(userRequest.getClientRegistration().getRegistrationId().equals("kakao"))
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        else if(userRequest.getClientRegistration().getRegistrationId().equals("naver"))
            oAuth2UserInfo = new NaverUserInfo((Map) oAuth2User.getAttributes().get("response"));
        else log.info("지원하지 않는 소셜입니다.");

        assert oAuth2UserInfo != null;
        Optional<User> userEntity = userService.findUserByProviderAndProviderId(oAuth2UserInfo.getProvider(), oAuth2UserInfo.getProviderId());

        User user = null;
        String role = "OAUTH_FIRST_JOIN";
        if (userEntity.isPresent()) {
            user = userEntity.get();
            userService.saveUser(user);
            if(user.getPrivacyAgreedAt()!=null) role="USER";
            // jwt 생성
        } else {
            user = userService.saveSocialUser(oAuth2UserInfo);
        }

        return new PrincipalUserDetail(user, oAuth2User.getAttributes(), Role.getIncludingRoles(role));
    }
}