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
}