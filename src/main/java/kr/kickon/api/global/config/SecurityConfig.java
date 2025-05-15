package kr.kickon.api.global.config;

import kr.kickon.api.global.auth.jwt.CustomAccessDeniedHandler;
import kr.kickon.api.global.auth.jwt.CustomAuthenticationEntryPoint;
import kr.kickon.api.global.auth.jwt.JwtAuthenticationFilter;
import kr.kickon.api.global.auth.oauth.CustomAuthorizationRequestResolver;
import kr.kickon.api.global.auth.oauth.CustomOAuth2FailureHandler;
import kr.kickon.api.global.auth.oauth.OAuth2SuccessHandler;
import kr.kickon.api.global.auth.oauth.PrincipalOauth2UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Slf4j
@AllArgsConstructor
public class SecurityConfig {
  private final PrincipalOauth2UserService principalOauth2UserService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;
  private final CustomAuthenticationEntryPoint authenticationEntryPoint;
  private final CustomAccessDeniedHandler accessDeniedHandler;
  private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll() // ✅ 모든 요청 허용
        )
        .oauth2Login(oAuth2Login -> oAuth2Login
            .userInfoEndpoint(userInfo -> userInfo.userService(principalOauth2UserService))
            .successHandler(oAuth2SuccessHandler)
            .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(customAuthorizationRequestResolver))
            .failureHandler(customOAuth2FailureHandler)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler)
        );

    return http.build();
  }
}
