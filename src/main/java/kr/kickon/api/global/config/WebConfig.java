package kr.kickon.api.global.config;

import kr.kickon.api.ApiApplication;
import kr.kickon.api.global.util.slack.SlackService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${spring.config.activate.on-profile}")
    private String env;
    @Override
    public void addCorsMappings(CorsRegistry registry){
        if(env.equals("dev")){
            registry.addMapping("/**")
                    .allowedOriginPatterns("*") // “*“같은 와일드카드를 사용
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP method
                    .allowCredentials(true); // 쿠키 인증 요청 허용
        }if(env.equals("prod")){
            registry.addMapping("/**")
                    .allowedOrigins("https://kick-on.kr")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP method
                    .allowCredentials(true); // 쿠키 인증 요청 허용
        }
    }
}