package kr.kickon.api.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieConfig {
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public boolean isSecure() {
        return "prod".equals(activeProfile);
    }
}