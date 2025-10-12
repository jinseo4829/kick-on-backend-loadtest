package kr.kickon.api.global.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
@Slf4j
@ConfigurationProperties(prefix = "cookie")
public class CookieConfig {
    private boolean secure;
    private String domain;
    private String sameSite;
    @Value("${spring.profiles.active:local}")
    private String activeProfile;
    /**
     * 요청의 Origin을 보고 동적으로 domain 결정
     */
    public String getDomain(HttpServletRequest request) {
        // local 프로파일은 항상 localhost
        if ("local".equals(activeProfile)) {
            return "localhost";
        }

        // dev/prod 프로파일에서는 Origin 확인
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        // Origin 또는 Referer에서 localhost 확인
        boolean isLocalhost = (origin != null && origin.contains("localhost")) ||
                (referer != null && referer.contains("localhost"));

        if (isLocalhost) {
            log.info("🔵 [CookieConfig] 로컬 요청 감지 - domain: localhost");
            return "localhost";
        }

        // 기본값은 yml 설정 사용 (.kick-on.kr)
        log.info("🔵 [CookieConfig] Dev/Prod 요청 감지 - domain: {}", domain);
        return domain;
    }
}