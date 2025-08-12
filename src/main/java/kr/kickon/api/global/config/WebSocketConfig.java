package kr.kickon.api.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 1. 클라이언트가 메시지를 보낼 때 사용할 prefix
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 프론트가 구독할 주소 prefix (→ 여기로 메시지 보내면 클라이언트가 받음)
        registry.enableSimpleBroker("/topic");

//        // 프론트가 서버에 메시지 보낼 때 붙이는 prefix (우리는 사용 안할 수도 있음)
//        registry.setApplicationDestinationPrefixes("/app");
    }

    // 2. 소켓 연결 endpoint 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 소켓 연결 경로: 예) wss://kick-on.kr/ws
        registry.addEndpoint("/ws")
                .setAllowedOrigins( "https://dev.kick-on.kr",
                                    "https://kick-on.kr",
                                    "https://api-dev.kick-on.kr",
                                    "http://localhost:3000")// 실서비스는 origin 제한 필요
                .withSockJS();          // SockJS fallback 지원
    }
}
