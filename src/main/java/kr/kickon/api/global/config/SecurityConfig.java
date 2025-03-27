package kr.kickon.api.global.config;

import kr.kickon.api.global.auth.jwt.CustomAccessDeniedHandler;
import kr.kickon.api.global.auth.jwt.CustomAuthenticationEntryPoint;
import kr.kickon.api.global.auth.jwt.JwtAuthenticationFilter;
import kr.kickon.api.global.auth.oauth.CustomAuthorizationRequestResolver;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .httpBasic(AbstractHttpConfigurer::disable) // ui 사용하는거 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                .csrf(AbstractHttpConfigurer::disable) // CSRF 보안 비활성화
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionConfig)->{
                    sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                }) // 세션 관리 정책
                .authorizeHttpRequests((authorizeRequests) -> {
                    authorizeRequests
                            .requestMatchers("/swagger-ui/*", "/oauth2/*", "/v3/**").permitAll() // ✅ OAuth2 로그인 경로, swagger 호출 허용
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                            .requestMatchers(HttpMethod.GET,
                                    "/api/user/me",
                                    "/api/user-point-event/ranking"
                            ).hasRole("USER")
                            .requestMatchers(HttpMethod.POST,
                                    "/api/user-game-gamble",
                                    "/api/board",
                                    "/api/news",
                                    "/api/board-reply",
                                    "/api/news-reply",
                                    "/api/report-news",
                                    "/api/report-board",
                                    "/api/board-reply-kick",
                                    "/api/news-reply-kick",
                                    "/api/news-kick",
                                    "/api/board-kick"
                            ).hasRole("USER")
                            .requestMatchers(HttpMethod.PATCH,
                                    "/api/user-game-gamble"
                            )
                            .hasRole("USER")
                            .requestMatchers(HttpMethod.DELETE,
                                    "/api/user-game-gamble"
                            ).hasRole("USER")

                            .requestMatchers(HttpMethod.PATCH,"/api/user").hasAnyRole("OAUTH_FIRST_JOIN", "USER")
                            .requestMatchers(HttpMethod.PATCH,"/api/user/privacy").hasAnyRole("OAUTH_FIRST_JOIN", "USER")

                            .requestMatchers("/api/**").hasAnyRole("GUEST", "OAUTH_FIRST_JOIN", "USER") // "GUEST"는 내부적으로 "ROLE_GUEST"로 변환됨

                            .requestMatchers("/**").permitAll()
                            .anyRequest().authenticated(); // ✅ 인증 필요
                })
                .oauth2Login(oAuth2Login -> {
                    oAuth2Login.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(principalOauth2UserService)).successHandler(oAuth2SuccessHandler).authorizationEndpoint(endpoint -> endpoint
                            .authorizationRequestResolver(customAuthorizationRequestResolver));
                        }
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // JWT 인증 실패 (401) → Custom EntryPoint
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );


        return http.build();
    }

}