package com.example.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        /*
         * setApplicationDestinationPrefixes
         * 도착 경로에 대한 prefix를 설정
         * /app : /topic/chat 이라는 구독을 신청했을 때 실제 경로는 /app/topic/chat
         */
        config.setApplicationDestinationPrefixes("/app");
        /*
         * enableSimpleBroker
         * 메시지 브로커 등록
         * 네이밍 : 보통 broadcast는 /topic, 특정 유저에게 보내는 것은 /queue
         */
        config.enableSimpleBroker("/queue");
        /*
         * setUserDestinationPrefix
         * 특정 유저에게 보낼 때(convertAndSendToUser) prefix
         * default : /user
         */
        // config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /*
         * socket 연결 엔드포인트
         * 이번엔 ChatHandshakeHandler를 등록 해 ChatHandshakeHandler에서 principal 주입!
         */
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new ChatHandshakeHandler())
                .withSockJS();
    }

}