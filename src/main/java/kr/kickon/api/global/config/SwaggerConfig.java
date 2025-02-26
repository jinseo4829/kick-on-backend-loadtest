package kr.kickon.api.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;


@Configuration
@Slf4j
public class SwaggerConfig {

    @Bean
    public OpenAPI kickonAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        );


        // API 문서 정보 설정
        Info info = new Info()
                .title("kick-on API")
                .description("kick-on API 명세서")
                .version("0.1.0");

        return new OpenAPI()
                .addServersItem(new Server().url("/"))  // 기본 서버 설정
                .info(info)  // API 문서 정보 추가
                .addSecurityItem(securityRequirement).components(components);
    }
}