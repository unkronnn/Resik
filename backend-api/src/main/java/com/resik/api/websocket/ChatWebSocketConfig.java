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

    public ChatWebSocketConfig(ChatWebSocketHandler chatWebSocketHandler,
                               RealtimeEventHandler realtimeEventHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.realtimeEventHandler = realtimeEventHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*");
        // General realtime event channel (pickup status, wallet, prices,
        // verification, stock, leaderboard). Same plain-TextWebSocketHandler
        // pattern as chat; /ws/** is permitAll in SecurityConfig.
        registry.addHandler(realtimeEventHandler, "/ws/events")
                .setAllowedOrigins("*");
    }
}
