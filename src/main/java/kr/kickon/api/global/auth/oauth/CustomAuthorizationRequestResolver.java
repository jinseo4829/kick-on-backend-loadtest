package kr.kickon.api.global.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import kr.kickon.api.global.common.enums.ResponseCode;
import org.apache.coyote.BadRequestException;
import org.apache.coyote.Response;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultAuthorizationRequestResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultAuthorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
    }

    /**
     * 기본 Resolver를 통해 요청 처리
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = this.defaultAuthorizationRequestResolver.resolve(request);
        try {
            return customizeAuthorizationRequest(request, authorizationRequest);
        } catch (BadRequestException e) {
            throw new kr.kickon.api.global.error.exceptions.BadRequestException(ResponseCode.PARAMETER_NOT_EXIST);
        }
    }

    /**
     * 기본 Resolver를 통해 요청 처리
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = this.defaultAuthorizationRequestResolver.resolve(request, clientRegistrationId);
        try {
            return customizeAuthorizationRequest(request, authorizationRequest);
        } catch (BadRequestException e) {
            throw new kr.kickon.api.global.error.exceptions.BadRequestException(ResponseCode.PARAMETER_NOT_EXIST);
        }
    }

    /**
     * OAuth2 인증 요청에 state 파라미터를 추가하는 메서드
     */
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest) throws BadRequestException {
        if (authorizationRequest == null) {
            return null;
        }

        String state = request.getParameter("state");

        // state가 있을 때만 커스터마이징
        if (StringUtils.hasText(state)) {
            return OAuth2AuthorizationRequest.from(authorizationRequest)
                    .state(state)  // 프론트가 보낸 state 사용
                    .build();
        }

        /*
        if (!StringUtils.hasText(state)) {
            throw new kr.kickon.api.global.error.exceptions.BadRequestException(ResponseCode.PARAMETER_NOT_EXIST);
        }*/

        // state가 없으면 원본 그대로
        return authorizationRequest;

        /*
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .state(state)
                .build();
         */
    }
}