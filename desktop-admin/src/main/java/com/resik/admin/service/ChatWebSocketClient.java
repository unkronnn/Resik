package com.resik.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * WebSocket client for receiving real-time chat notifications from the backend.
 * Uses Java 11+ built-in java.net.http.WebSocket — no extra dependencies needed.
 */
public class ChatWebSocketClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    private WebSocket webSocket;
    private final String wsUrl;
    private final Consumer<Map<String, Object>> messageHandler;
    private final Runnable onConnect;
    private final Runnable onDisconnect;
    private volatile boolean running = false;
    private final java.util.concurrent.ScheduledExecutorService scheduler =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ws-reconnect");
                t.setDaemon(true);
                return t;
            });

    public ChatWebSocketClient(String apiUrl, Consumer<Map<String, Object>> messageHandler,
                               Runnable onConnect, Runnable onDisconnect) {
        this.wsUrl = apiUrl.replace("http://", "ws://").replace("https://", "wss://")
                + "/ws/chat?role=admin";
        this.messageHandler = messageHandler;
        this.onConnect = onConnect;
        this.onDisconnect = onDisconnect;
    }

    public void connect() {
        running = true;
        doConnect();
    }

    private void doConnect() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            CompletableFuture<WebSocket> wsFuture = client.newWebSocketBuilder()
                    .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {

                        @Override
                        public void onOpen(WebSocket webSocket) {
                            WebSocket.Listener.super.onOpen(webSocket);
                            if (onConnect != null) onConnect.run();
                            // Start sending ping every 30s keepalive
                            scheduler.scheduleAtFixedRate(() -> {
                                try {
                                    if (webSocket != null) {
                                        webSocket.sendText("{\"type\":\"ping\"}", true);
                                    }
                                } catch (Exception ignored) {}
                            }, 30, 30, java.util.concurrent.TimeUnit.SECONDS);
                        }

                        @Override
                        public java.util.concurrent.CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            try {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> msg = mapper.readValue(data.toString(), Map.class);
                                if (messageHandler != null) {
                                    messageHandler.accept(msg);
                                }
                            } catch (Exception e) {
                                // Ignore parse errors (e.g. pong)
                            }
                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }

                        @Override
                        public void onError(WebSocket webSocket, Throwable error) {
                            WebSocket.Listener.super.onError(webSocket, error);
                            handleDisconnect();
                        }

                        @Override
                        public java.util.concurrent.CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                            handleDisconnect();
                            return null;
                        }
                    });

            // Use thenAccept/error handling instead of join() to avoid blocking
            wsFuture.thenAccept(ws -> {
                this.webSocket = ws;
            }).exceptionally(ex -> {
                handleDisconnect();
                return null;
            });
        } catch (Exception e) {
            handleDisconnect();
        }
    }

    private void handleDisconnect() {
        if (onDisconnect != null) onDisconnect.run();
        if (running) {
            // Reconnect after 3 seconds
            scheduler.schedule(this::doConnect, 3, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    public void disconnect() {
        running = false;
        scheduler.shutdownNow();
        if (webSocket != null) {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client disconnect");
            } catch (Exception ignored) {}
        }
    }
}
