package kr.kickon.api.global.config;

import kr.kickon.api.global.common.filters.CachingRequestBodyFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<CachingRequestBodyFilter> loggingFilter() {
        FilterRegistrationBean<CachingRequestBodyFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CachingRequestBodyFilter());
        registrationBean.addUrlPatterns("/api/*", "/migration/*", "/oauth2/*"); // 필터를 적용할 URL 패턴 지정
        registrationBean.setOrder(1); // AOP보다 먼저 실행되도록 설정
        return registrationBean;
    }
}