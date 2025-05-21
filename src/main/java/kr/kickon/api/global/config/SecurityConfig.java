package kr.kickon.api.global.config;

import kr.kickon.api.global.auth.jwt.CustomAccessDeniedHandler;
import kr.kickon.api.global.auth.jwt.CustomAuthenticationEntryPoint;
import kr.kickon.api.global.auth.jwt.admin.AdminJwtAuthenticationFilter;
import kr.kickon.api.global.auth.jwt.user.JwtAuthenticationFilter;
import kr.kickon.api.global.auth.oauth.CustomAuthorizationRequestResolver;
import kr.kickon.api.global.auth.oauth.CustomOAuth2FailureHandler;
import kr.kickon.api.global.auth.oauth.OAuth2SuccessHandler;
import kr.kickon.api.global.auth.oauth.PrincipalOauth2UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@Slf4j
@AllArgsConstructor
public class SecurityConfig {
    private final PrincipalOauth2UserService principalOauth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AdminJwtAuthenticationFilter adminJwtAuthenticationFilter;
    private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

    private void configureCommonSecuritySettings(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)
                .headers(options -> options.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChainPermitAll(HttpSecurity http) throws Exception {
        configureCommonSecuritySettings(http);
        http.securityMatchers(matchers -> matchers.requestMatchers(requestPermitAll()))
                .authorizeHttpRequests(auth -> auth.requestMatchers(requestPermitAll()).permitAll());
        return http.build();
    }

    private RequestMatcher[] requestPermitAll() {
        List<RequestMatcher> requestMatchers = List.of(
                antMatcher("/swagger-ui/**"),
                antMatcher("/swagger-ui.html"), // 경우에 따라 필요
                antMatcher("/v3/api-docs/**"),
                antMatcher("/swagger-resources/**"),
                antMatcher("/webjars/**"),
                antMatcher(HttpMethod.POST, "/admin/auth/login"),
                antMatcher(HttpMethod.POST, "/aws/presigned-url"),
                antMatcher(HttpMethod.OPTIONS, "/**")
                );
        return requestMatchers.toArray(RequestMatcher[]::new);
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChainOAuth(HttpSecurity http) throws Exception {
        configureCommonSecuritySettings(http);
        http
                .securityMatchers(matchers -> matchers
                        .requestMatchers(
                                antMatcher("/login/oauth2/code/kakao"),
                                antMatcher("/oauth2/authorization/kakao"),
                                antMatcher("/login/oauth2/code/naver"),
                                antMatcher("/oauth2/authorization/naver")
                        ))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oAuth2Login -> {
                    oAuth2Login.userInfoEndpoint(
                            userInfoEndpointConfig -> userInfoEndpointConfig.userService(principalOauth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .authorizationEndpoint(endpoint -> endpoint
                            .authorizationRequestResolver(customAuthorizationRequestResolver)
                        )
                        .failureHandler(customOAuth2FailureHandler);
                    }
                );
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        configureCommonSecuritySettings(http);
        http.securityMatchers(matchers -> matchers.requestMatchers(requestHasRoleAdmin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(requestHasRoleAdmin()).hasAnyRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterAfter(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    // ADMIN 권한이 필요한 엔드포인트
    private RequestMatcher[] requestHasRoleAdmin() {
        List<RequestMatcher> requestMatchers = List.of(
                antMatcher( "/admin/**")
		);
        return requestMatchers.toArray(RequestMatcher[]::new);
    }

    @Bean
    @Order(4)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureCommonSecuritySettings(http);
        http
                .securityMatchers(matchers->matchers
                        .requestMatchers(requestUserMatchers())
                        .requestMatchers(requestOauthFirstJoinMatchers()))
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(requestUserMatchers()).hasAnyRole("USER")
                                .requestMatchers(requestOauthFirstJoinMatchers()).hasAnyRole("OAUTH_FIRST_JOIN")
                                .requestMatchers("/api/**").hasAnyRole("GUEST", "OAUTH_FIRST_JOIN", "USER") // "GUEST"는 내부적으로 "ROLE_GUEST"로 변환됨
                                .anyRequest().authenticated()

                )
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    // User 권한이 필요한 엔드포인트
    private RequestMatcher[] requestUserMatchers() {
        List<RequestMatcher> requestMatchers = List.of(
                antMatcher(HttpMethod.GET, "/api/user/me"),
                antMatcher(HttpMethod.GET, "/api/user-point-event/ranking"),

                antMatcher(HttpMethod.POST, "/api/user-game-gamble"),
                antMatcher(HttpMethod.POST, "/api/board"),
                antMatcher(HttpMethod.POST, "/api/news"),
                antMatcher(HttpMethod.POST, "/api/board-reply"),
                antMatcher(HttpMethod.POST, "/api/news-reply"),
                antMatcher(HttpMethod.POST, "/api/report-news"),
                antMatcher(HttpMethod.POST, "/api/report-board"),
                antMatcher(HttpMethod.POST, "/api/board-reply-kick"),
                antMatcher(HttpMethod.POST, "/api/news-reply-kick"),
                antMatcher(HttpMethod.POST, "/api/news-kick"),
                antMatcher(HttpMethod.POST, "/api/board-kick"),

                antMatcher(HttpMethod.PATCH, "/api/user-game-gamble"),
                antMatcher(HttpMethod.PATCH, "/api/user"),
                antMatcher(HttpMethod.PATCH, "/api/user/privacy"),

                antMatcher(HttpMethod.DELETE, "/api/user-game-gamble"),
                antMatcher(HttpMethod.DELETE, "/api/user/me")
        );
        return requestMatchers.toArray(RequestMatcher[]::new);
    }

    // OAUTH_FIRST_JOIN 권한이 필요한 엔드포인트
    private RequestMatcher[] requestOauthFirstJoinMatchers() {
        List<RequestMatcher> requestMatchers = List.of(
                antMatcher(HttpMethod.DELETE,"/api/user/me"),
                antMatcher(HttpMethod.GET,"/api/user/me"),
                antMatcher(HttpMethod.DELETE,"/api/user/me"),
                antMatcher(HttpMethod.DELETE,"/api/user-point-event/ranking")
        );
        return requestMatchers.toArray(RequestMatcher[]::new);
    }

}