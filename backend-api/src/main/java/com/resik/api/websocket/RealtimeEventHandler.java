package com.resik.api.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * WebSocket handler for the general realtime event channel ({@code /ws/events}).
 *
 * <p>Identity comes from the login JWT validated by
 * {@link JwtHandshakeInterceptor} at handshake time and stored in the
 * session attributes ({@code userId}, {@code role}). The old
 * {@code ?role=} / {@code ?userId=} query parameters are ignored:
 *
 * <ul>
 *   <li>role {@code ADMIN} — admin desktop clients, receive every event.</li>
 *   <li>role {@code PETUGAS} — bank sampah / petugas clients, receive
 *       pickup, stock and price events (but no per-user wallet events).</li>
 *   <li>any other role — masyarakat (Android) clients, receive broadcast
 *       events plus events addressed to their own user id (from the token).</li>
 * </ul>
 *
 * <p>Clients answer with a {@code ping} keepalive; the handler replies
 * {@code {"type":"pong"}} exactly like the chat handler.
 */
@Component
public class RealtimeEventHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(RealtimeEventHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();

    // Sessions grouped by audience, mirroring ChatWebSocketHandler.
    private final Set<WebSocketSession> adminSessions = new CopyOnWriteArraySet<>();
    private final Set<WebSocketSession> bankSampahSessions = new CopyOnWriteArraySet<>();
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Identity is taken from the JWT (see JwtHandshakeInterceptor);
        // query parameters are never trusted for role or user id.
        Long userId = (Long) session.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
        String role = (String) session.getAttributes().get(JwtHandshakeInterceptor.ATTR_ROLE);
        if (userId == null) {
            log.warn("Realtime-events connection without authenticated identity, closing: {}", session.getId());
            try {
                session.close(CloseStatus.POLICY_VIOLATION);
            } catch (IOException ignored) {
            }
            return;
        }
        if ("ADMIN".equalsIgnoreCase(role)) {
            adminSessions.add(session);
            log.info("Admin realtime-events connected: {}", session.getId());
        } else if ("PETUGAS".equalsIgnoreCase(role) || "BANK_SAMPAH".equalsIgnoreCase(role)) {
            bankSampahSessions.add(session);
            log.info("Bank Sampah realtime-events connected: {}", session.getId());
        } else {
            userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
            log.info("User {} realtime-events connected: {}", userId, session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        adminSessions.remove(session);
        bankSampahSessions.remove(session);
        userSessions.values().forEach(s -> s.remove(session));
        log.info("Realtime-events disconnected: {} (status: {})", session.getId(), status);
    }

    /** Sends the event to every connected session, regardless of role. */
    public void broadcastToAll(RealtimeEvent event) {
        TextMessage message = toMessage(event);
        if (message == null) return;
        adminSessions.forEach(s -> sendMessage(s, message));
        bankSampahSessions.forEach(s -> sendMessage(s, message));
        userSessions.values().forEach(sessions -> sessions.forEach(s -> sendMessage(s, message)));
    }

    /** Sends the event to admin sessions only. */
    public void broadcastToAdmins(RealtimeEvent event) {
        TextMessage message = toMessage(event);
        if (message == null) return;
        adminSessions.forEach(s -> sendMessage(s, message));
    }

    /** Sends the event to admin and bank sampah sessions. */
    public void broadcastToAdminsAndBankSampah(RealtimeEvent event) {
        TextMessage message = toMessage(event);
        if (message == null) return;
        adminSessions.forEach(s -> sendMessage(s, message));
        bankSampahSessions.forEach(s -> sendMessage(s, message));
    }

    /** Sends the event to one user only (all of their sessions). */
    public void broadcastToUser(Long userId, RealtimeEvent event) {
        if (userId == null) return;
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null) return;
        TextMessage message = toMessage(event);
        if (message == null) return;
        sessions.forEach(s -> sendMessage(s, message));
    }

    /** Sends the event to admins and to one user (the owner of the subject). */
    public void broadcastToAdminsAndUser(Long userId, RealtimeEvent event) {
        broadcastToAdmins(event);
        broadcastToUser(userId, event);
    }

    /** Sends the event to admins, bank sampah, and one user. */
    public void broadcastToAdminsBankSampahAndUser(Long userId, RealtimeEvent event) {
        broadcastToAdminsAndBankSampah(event);
        broadcastToUser(userId, event);
    }

    private TextMessage toMessage(RealtimeEvent event) {
        try {
            return new TextMessage(mapper.writeValueAsString(event.toWireMap()));
        } catch (Exception e) {
            log.error("Failed to serialize realtime event {}", event.getType(), e);
            return null;
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
            log.warn("Failed to send realtime event to {}: {}", session.getId(), e.getMessage());
        }
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
