package com.resik.client.api

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import okhttp3.*
import okio.ByteString
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * WebSocket client for the backend realtime event channel ({@code /ws/events}).
 *
 * Mirrors [ChatWebSocketClient] exactly (OkHttp WebSocket, 30s ping,
 * auto-reconnect after 3s, callbacks on the main thread) so both channels
 * behave the same way. The only differences are the URL and that messages
 * are parsed into [RealtimeEvent] instead of a raw map.
 *
 * Use [RealtimeEventHub] instead of this class directly: the hub keeps one
 * shared connection per app process and fans events out to listeners, so
 * every screen sees the same live state without opening extra sockets.
 */
class EventWebSocketClient(private val wsUrl: String) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // No timeout for WebSocket
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var isConnected = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val gson = Gson()

    private var onConnectCb: (() -> Unit)? = null
    private var onDisconnectCb: (() -> Unit)? = null
    private var onEventCb: ((RealtimeEvent) -> Unit)? = null

    private val reconnectRunnable = Runnable {
        onConnectCb?.let { cb1 ->
            onDisconnectCb?.let { cb2 ->
                onEventCb?.let { cb3 ->
                    connect(cb1, cb2, cb3)
                }
            }
        }
    }

    fun connect(
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onEvent: (RealtimeEvent) -> Unit
    ) {
        this.onConnectCb = onConnect
        this.onDisconnectCb = onDisconnect
        this.onEventCb = onEvent

        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                mainHandler.post { onConnectCb?.invoke() }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleTextMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                handleTextMessage(bytes.utf8())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                handleDisconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                handleDisconnect()
            }
        })
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleTextMessage(text: String) {
        try {
            val msg = gson.fromJson(text, Map::class.java) as Map<String, Any>
            // Pong keepalive replies have no payload; ignore them.
            val event = RealtimeEvent.fromMap(msg) ?: return
            mainHandler.post { onEventCb?.invoke(event) }
        } catch (e: Exception) {
            // Ignore parse errors (e.g. pong responses)
        }
    }

    private fun handleDisconnect() {
        isConnected = false
        mainHandler.post { onDisconnectCb?.invoke() }
        // Auto-reconnect after 3 seconds
        mainHandler.postDelayed(reconnectRunnable, 3000)
    }

    fun disconnect() {
        mainHandler.removeCallbacks(reconnectRunnable)
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        isConnected = false
    }
}

/**
 * Process-wide hub for the realtime event channel.
 *
 * One shared [EventWebSocketClient] connection; screens register a listener
 * with a stable key and remove it when they leave (same pattern as the chat
 * screen's DisposableEffect cleanup). Events are delivered on the main
 * thread. If the socket is down, screens keep showing their last REST-loaded
 * data — the event channel is an update mechanism, never the only data source.
 */
object RealtimeEventHub {

    private var client: EventWebSocketClient? = null
    private val listeners = ConcurrentHashMap<String, (RealtimeEvent) -> Unit>()
    private val connectListeners = ConcurrentHashMap<String, () -> Unit>()
    private var connectedUserId: Long? = null

    @Volatile
    var isConnected: Boolean = false
        private set

    fun connect(userId: Long, role: String) {
        if (client != null && connectedUserId == userId) return
        disconnect()
        connectedUserId = userId
        val baseUrl = ApiClient.getBaseUrl()
        val jwt = ApiClient.getToken()
        val wsUrl = baseUrl.replace("http://", "ws://").replace("https://", "wss://")
            .replace("/api/", "/ws/") +
            "events?userId=$userId&role=${role.lowercase()}" +
            (if (!jwt.isNullOrBlank()) "&token=$jwt" else "")
        val ws = EventWebSocketClient(wsUrl)
        client = ws
        ws.connect(
            onConnect = {
                isConnected = true
                // Beri tahu pemicu sync offline (butir 7): koneksi pulih.
                connectListeners.values.forEach { listener ->
                    try {
                        listener()
                    } catch (e: Exception) {
                    }
                }
            },
            onDisconnect = { isConnected = false },
            onEvent = { event ->
                listeners.values.forEach { listener ->
                    try {
                        listener(event)
                    } catch (e: Exception) {
                        // One bad listener must not break the others.
                    }
                }
            }
        )
    }

    /** Register (or replace) a listener. The key is usually the screen name. */
    fun addListener(key: String, listener: (RealtimeEvent) -> Unit) {
        listeners[key] = listener
    }

    fun removeListener(key: String) {
        listeners.remove(key)
    }

    /**
     * Listener yang dipanggil setiap kali kanal /ws/events (kembali)
     * tersambung — dipakai OfflineQueue untuk mengirim ulang antrean
     * saat koneksi pulih, bukan hanya saat aplikasi dibuka.
     */
    fun addConnectListener(key: String, listener: () -> Unit) {
        connectListeners[key] = listener
    }

    fun removeConnectListener(key: String) {
        connectListeners.remove(key)
    }

    fun disconnect() {
        client?.disconnect()
        client = null
        connectedUserId = null
        isConnected = false
    }
}
