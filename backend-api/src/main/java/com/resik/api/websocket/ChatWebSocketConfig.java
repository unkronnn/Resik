package com.resik.api.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final RealtimeEventHandler realtimeEventHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public ChatWebSocketConfig(ChatWebSocketHandler chatWebSocketHandler,
                               RealtimeEventHandler realtimeEventHandler,
                               JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.realtimeEventHandler = realtimeEventHandler;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Both channels require a valid login JWT at handshake time (see
        // JwtHandshakeInterceptor). Identity comes from the token, never
        // from ?role= / ?userId= query parameters.
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("*");
        // General realtime event channel (pickup status, wallet, prices,
        // verification, stock, leaderboard). Same plain-TextWebSocketHandler
        // pattern as chat; /ws/** is permitAll in SecurityConfig because
        // the handshake interceptor performs the JWT check.
        registry.addHandler(realtimeEventHandler, "/ws/events")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
