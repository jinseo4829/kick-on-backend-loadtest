package kr.kickon.api.global.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.web.method.HandlerMethod;

import java.util.HashMap;
import java.util.Map;


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

//    @Bean
//    public OperationCustomizer operationCustomizer() {
//        return (Operation operation, HandlerMethod handlerMethod) -> {
//            // 메서드의 반환 타입 가져오기
//            Class<?> returnType = handlerMethod.getMethod().getReturnType();
//
//            // 반환 타입이 Void가 아니면 API 응답 추가
//            if (!Void.TYPE.equals(returnType)) {
//                ApiResponses responses = operation.getResponses();
//                ApiResponse response = new ApiResponse().description("Successfully processed the request");
//
//                // 응답 스키마 설정
//                Schema<?> schema = new Schema<>();
//                schema.set$ref("#/components/schemas/" + returnType.getSimpleName());
//                Map map = new HashMap();
//                response.setContent((Content) map.put("application/json",
//                        new Content().addMediaType("application/json",
//                                new MediaType().schema(schema))));
//
//                responses.addApiResponse("200", response);
//            }
//            return operation;
//        };
//    }
}