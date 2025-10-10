package kr.kickon.api.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthConfigDebugger {
    
    private final ClientRegistrationRepository clientRegistrationRepository;
    
    @Value("${spring.profiles.active:}")
    private String activeProfile;
    
    @Value("${KAKAO_REDIRECT_URI:}")
    private String kakaoRedirectUri;
    
    @Value("${KAKAO_CLIENT:}")
    private String kakaoClientId;
    
    @PostConstruct
    public void debugOAuthConfig() {
        log.info("🔍 OAuth 설정 디버깅 시작");
        log.info("   📋 활성 프로필: {}", activeProfile);
        log.info("   🔗 KAKAO_REDIRECT_URI: {}", kakaoRedirectUri);
        log.info("   🆔 KAKAO_CLIENT: {}", kakaoClientId);
        
        // ClientRegistrationRepository 확인
        try {
            var kakaoRegistration = clientRegistrationRepository.findByRegistrationId("kakao");
            if (kakaoRegistration != null) {
                log.info("✅ Kakao OAuth 등록 발견:");
                log.info("   - Client ID: {}", kakaoRegistration.getClientId());
                log.info("   - Redirect URI: {}", kakaoRegistration.getRedirectUri());
                log.info("   - Authorization URI: {}", kakaoRegistration.getProviderDetails().getAuthorizationUri());
                log.info("   - Token URI: {}", kakaoRegistration.getProviderDetails().getTokenUri());
                log.info("   - User Info URI: {}", kakaoRegistration.getProviderDetails().getUserInfoEndpoint().getUri());
            } else {
                log.error("❌ Kakao OAuth 등록을 찾을 수 없습니다!");
            }
        } catch (Exception e) {
            log.error("❌ OAuth 설정 확인 중 오류 발생: {}", e.getMessage(), e);
        }
        
        // 환경변수 직접 확인
        String envKakaoRedirect = System.getenv("KAKAO_REDIRECT_URI");
        String envKakaoClient = System.getenv("KAKAO_CLIENT");
        log.info("   🌍 System.getenv('KAKAO_REDIRECT_URI'): {}", envKakaoRedirect);
        log.info("   🌍 System.getenv('KAKAO_CLIENT'): {}", envKakaoClient);
        
        // 시스템 프로퍼티도 확인
        String sysPropKakaoRedirect = System.getProperty("KAKAO_REDIRECT_URI");
        log.info("   🔧 System.getProperty('KAKAO_REDIRECT_URI'): {}", sysPropKakaoRedirect);
        
        // 모든 KAKAO 관련 환경변수 확인
        System.getenv().entrySet().stream()
                .filter(entry -> entry.getKey().toUpperCase().contains("KAKAO"))
                .forEach(entry -> log.info("   🔍 환경변수 {} = {}", entry.getKey(), entry.getValue()));
    }
}
