package kr.kickon.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI kickonAPI() {

        // API 문서 정보 설정
        Info info = new Info()
                .title("kick-on API")
                .description("kick-on API 명세서")
                .version("0.1.0");

        // Security Scheme 정의 (userId 기반 인증)
        SecurityScheme userIdScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)  // API 키 방식
                .in(SecurityScheme.In.HEADER)  // 헤더에서 userId를 받음
                .name("userId");  // Swagger에서 사용할 인증 키 이름

        return new OpenAPI()
                .addServersItem(new Server().url("/"))  // 기본 서버 설정
                .info(info)  // API 문서 정보 추가
                .addSecurityItem(new SecurityRequirement().addList("userId"))  // 보안 요구 사항 추가
                .components(new Components().addSecuritySchemes("userId", userIdScheme)); // 보안 스키마 설정
    }
}