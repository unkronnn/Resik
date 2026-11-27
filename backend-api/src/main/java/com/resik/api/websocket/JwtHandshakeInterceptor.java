package com.resik.api.websocket;

import com.resik.api.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * Validates the login JWT during the WebSocket handshake for {@code /ws/chat}
 * and {@code /ws/events}, using the same {@link JwtUtil} (same secret,
 * same claims) as the REST {@code JwtAuthFilter}.
 *
 * <p>Clients must supply the token they received from {@code POST /api/auth/login}
 * either as a query parameter ({@code ?token=&lt;jwt&gt;}) or as an
 * {@code Authorization: Bearer &lt;jwt&gt;} header. The query parameter is
 * the practical route: browsers cannot set custom headers on a WebSocket
 * handshake. The handshake is rejected when the token is missing, expired,
 * or fails signature validation.
 *
 * <p>On success the interceptor stores the identity <em>from the token</em>
 * in the session attributes ({@code userId}, {@code role}, {@code email}).
 * The handlers group sessions from those attributes only — the old
 * {@code ?role=} / {@code ?userId=} query parameters are ignored, so a
 * client can no longer impersonate another user or an admin by editing
 * the URL.
 *
 * <p>Note: the token travels in the handshake URL when the query-param
 * route is used, so it can appear in access logs / proxy logs. Clients
 * must use {@code wss://} in production, and the token lifetime is the
 * same 24h as the REST token (see {@code jwt.expiration}). A future
 * improvement is authenticating with the first message after connect
 * instead of the URL; see {@code docs/SECURITY.md}.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_ROLE = "role";
    public static final String ATTR_EMAIL = "email";

    private final JwtUtil jwtUtil;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            log.warn("WebSocket handshake rejected: no token ({})", request.getURI().getPath());
            return false;
        }
        if (!jwtUtil.validateToken(token)) {
            log.warn("WebSocket handshake rejected: invalid/expired token ({})", request.getURI().getPath());
            return false;
        }
        try {
            attributes.put(ATTR_USER_ID, jwtUtil.getUserIdFromToken(token));
            attributes.put(ATTR_ROLE, jwtUtil.getRoleFromToken(token));
            attributes.put(ATTR_EMAIL, jwtUtil.getEmailFromToken(token));
        } catch (Exception e) {
            log.warn("WebSocket handshake rejected: unreadable token claims ({})", request.getURI().getPath());
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Nothing to do after the handshake.
    }

    /**
     * Prefers the {@code Authorization: Bearer} header; falls back to the
     * {@code token} query parameter (the only option for browser WebSocket
     * clients).
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        URI uri = request.getURI();
        String query = uri != null ? uri.getRawQuery() : null;
        if (query == null) return null;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                return param.substring("token=".length());
            }
        }
        return null;
    }
}
