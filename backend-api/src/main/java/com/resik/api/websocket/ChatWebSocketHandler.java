package com.resik.api.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resik.api.entity.Chat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();

    // Sessions grouped by role: "admin" or "user:{userId}"
    private final Set<WebSocketSession> adminSessions = new CopyOnWriteArraySet<>();
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String role = extractRole(session);
        if ("admin".equals(role)) {
            adminSessions.add(session);
            log.info("Admin WebSocket connected: {}", session.getId());
        } else {
            Long userId = extractUserId(session);
            if (userId != null) {
                userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
                log.info("User {} WebSocket connected: {}", userId, session.getId());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        adminSessions.remove(session);
        userSessions.values().forEach(s -> s.remove(session));
        log.info("WebSocket disconnected: {} (status: {})", session.getId(), status);
    }

    /**
     * Broadcast a new chat message to all connected admin sessions and to the
     * user involved. The message JSON includes userId so the admin UI can route it.
     */
    public void broadcastNewMessage(Chat chat) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "NEW_MESSAGE",
                    "id", chat.getId(),
                    "userId", chat.getUser().getId(),
                    "pengirim", chat.getPengirim(),
                    "pesan", chat.getPesan(),
                    "createdAt", chat.getCreatedAt() != null ? chat.getCreatedAt().toString() : ""
            );
            String json = mapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(json);

            // Notify all admin sessions
            for (WebSocketSession s : adminSessions) {
                sendMessage(s, message);
            }

            // Notify the user if they're connected
            Long userId = chat.getUser().getId();
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                for (WebSocketSession s : sessions) {
                    sendMessage(s, message);
                }
            }
        } catch (Exception e) {
            log.error("Failed to broadcast chat message", e);
        }
    }

    /**
     * Send a typed notification (e.g. admin typing indicator) to a specific user.
     */
    public void notifyUser(Long userId, Map<String, Object> payload) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null) return;
        try {
            String json = mapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(json);
            for (WebSocketSession s : sessions) {
                sendMessage(s, message);
            }
        } catch (Exception e) {
            log.error("Failed to notify user {}", userId, e);
        }
    }

    private void sendMessage(WebSocketSession session, TextMessage message) {
        try {
            if (session.isOpen()) {
                synchronized (session) {
                    session.sendMessage(message);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to send WebSocket message to {}: {}", session.getId(), e.getMessage());
        }
    }

    private String extractRole(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        if (query == null) return null;
        for (String param : query.split("&")) {
            if (param.startsWith("role=")) {
                return param.substring(5);
            }
        }
        return null;
    }

    private Long extractUserId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        if (query == null) return null;
        for (String param : query.split("&")) {
            if (param.startsWith("userId=")) {
                try {
                    return Long.parseLong(param.substring(8));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Clients can send "ping" keepalive; we just echo back
        try {
            session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
        } catch (IOException e) {
            log.warn("Failed to send pong to {}", session.getId());
        }
    }
}
