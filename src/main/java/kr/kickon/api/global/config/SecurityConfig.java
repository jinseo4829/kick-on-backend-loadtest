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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
                antMatcher(HttpMethod.POST,"/auth/refresh"),
                antMatcher(HttpMethod.POST,"/auth/admin/refresh"),
                antMatcher(HttpMethod.POST,"/auth/ai/login"),
                antMatcher(HttpMethod.OPTIONS, "/**")
                );
        return requestMatchers.toArray(RequestMatcher[]::new);
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChainOAuth(HttpSecurity http) throws Exception {
        log.info("🔐 OAuth SecurityFilterChain 설정 시작");
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
                    log.info("📋 OAuth2Login 설정 중...");
                    oAuth2Login.userInfoEndpoint(
                            userInfoEndpointConfig -> userInfoEndpointConfig.userService(principalOauth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .authorizationEndpoint(endpoint -> {
                            log.info("🔗 CustomAuthorizationRequestResolver 등록 중...");
                            endpoint.authorizationRequestResolver(customAuthorizationRequestResolver);
                        })
                        .failureHandler(customOAuth2FailureHandler);
                    }
                );
        log.info("✅ OAuth SecurityFilterChain 설정 완료");
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
//        log.info("✅ Entered adminSecurityFilterChain config");
        configureCommonSecuritySettings(http);
        http.securityMatchers(matchers -> matchers.requestMatchers(requestHasRoleAdmin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(requestHasRoleAdmin()).hasAnyRole("ADMIN")
                        .anyRequest().denyAll()
                )
                .addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
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
//        log.info("🛡️ Entered userSecurityFilterChain config");
        configureCommonSecuritySettings(http);
        http
                .securityMatchers(matchers->matchers
                        .requestMatchers(antMatcher("/api/**"))  // 꼭 api만!
                )
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(requestUserOauthFirstJoinMatchers()).hasAnyRole("USER", "OAUTH_FIRST_JOIN")
                                .requestMatchers(requestUserMatchers()).hasRole("USER")
                                .requestMatchers(requestOauthFirstJoinMatchers()).hasRole("OAUTH_FIRST_JOIN")
                                .requestMatchers("/api/**").hasAnyRole("GUEST", "OAUTH_FIRST_JOIN", "USER") // "GUEST"는 내부적으로 "ROLE_GUEST"로 변환됨
                                .anyRequest().authenticated()

                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    private RequestMatcher[] requestUserOauthFirstJoinMatchers() {
        List<RequestMatcher> requestMatchers = List.of(
                antMatcher(HttpMethod.PATCH, "/api/user"),
                antMatcher(HttpMethod.DELETE,"/api/user/me"),
                antMatcher(HttpMethod.GET,"/api/user/me"),
                antMatcher(HttpMethod.PATCH, "/api/user"),
                antMatcher(HttpMethod.DELETE,"/api/user-point-event/ranking"),
                antMatcher(HttpMethod.PATCH, "/api/user/privacy")
        );
        return requestMatchers.toArray(RequestMatcher[]::new);
    }

    // User 권한이 필요한 엔드포인트
    private RequestMatcher[] requestUserMatchers() {
        List<RequestMatcher> requestMatchers = List.of(
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
                antMatcher(HttpMethod.DELETE, "/api/user-game-gamble")
        );
        return requestMatchers.toArray(RequestMatcher[]::new);
    }

    // OAUTH_FIRST_JOIN 권한이 필요한 엔드포인트
    private RequestMatcher[] requestOauthFirstJoinMatchers() {
        List<RequestMatcher> requestMatchers = List.of(

        );
        return requestMatchers.toArray(RequestMatcher[]::new);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/ws/**",           // SockJS: /ws/info, /ws/**/websocket, /ws/**/xhr_streaming ...
                "/actuator/health"  // ALB/TG 헬스체크 쓰면 함께 제외 권장
        );
    }

}