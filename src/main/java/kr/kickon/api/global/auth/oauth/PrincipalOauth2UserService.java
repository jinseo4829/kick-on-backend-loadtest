package kr.kickon.api.global.auth.oauth;

import jakarta.transaction.Transactional;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.global.auth.oauth.dto.KakaoUserInfo;
import kr.kickon.api.global.auth.oauth.dto.NaverUserInfo;
import kr.kickon.api.global.auth.oauth.dto.OAuth2UserInfo;
import kr.kickon.api.global.auth.oauth.dto.PrincipalUserDetail;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.common.enums.Role;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.error.exceptions.OAuth2RegistrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        if(userRequest.getClientRegistration().getRegistrationId().equals("kakao"))
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        else if(userRequest.getClientRegistration().getRegistrationId().equals("naver"))
            oAuth2UserInfo = new NaverUserInfo((Map) oAuth2User.getAttributes().get("response"));
        else log.error("지원하지 않는 소셜입니다.");

        assert oAuth2UserInfo != null;
        Optional<User> userEntity = userService.findUserByProviderAndProviderId(oAuth2UserInfo.getProvider(), oAuth2UserInfo.getProviderId());

        User user = null;
        String role = "OAUTH_FIRST_JOIN";
        if (userEntity.isPresent()) {
            user = userEntity.get();
            LocalDateTime deactivatedAt = user.getUpdatedAt(); // 또는 탈퇴 기록 기준
            if (user.getStatus().equals(DataStatus.DEACTIVATED) && deactivatedAt != null && deactivatedAt.isAfter(LocalDateTime.now().minusDays(7))) {
                throw new OAuth2RegistrationException(ResponseCode.FORBIDDEN_RESISTER);
            }
            if(user.getPrivacyAgreedAt()!=null) role="USER";
//            log.error("jwt role 확인 {}", role);
            // jwt 생성
        } else {
            // 일주일 안에 탈퇴한 이력이 있는지 체크
            user = userService.saveSocialUser(oAuth2UserInfo);
        }

        return new PrincipalUserDetail(user, oAuth2User.getAttributes(), Role.getIncludingRoles(role));
    }
}
