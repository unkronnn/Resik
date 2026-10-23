package com.resik.client.api

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

/**
 * WebSocket client for receiving real-time chat messages from the backend.
 * Uses OkHttp WebSocket — already available via Retrofit dependency.
 */
class ChatWebSocketClient(private val wsUrl: String) {

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
    private var onMessageCb: ((Map<String, Any>) -> Unit)? = null

    private val reconnectRunnable = Runnable {
        onConnectCb?.let { cb1 ->
            onDisconnectCb?.let { cb2 ->
                onMessageCb?.let { cb3 ->
                    connect(cb1, cb2, cb3)
                }
            }
        }
    }

    fun connect(
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onMessage: (Map<String, Any>) -> Unit
    ) {
        this.onConnectCb = onConnect
        this.onDisconnectCb = onDisconnect
        this.onMessageCb = onMessage

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
            mainHandler.post { onMessageCb?.invoke(msg) }
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
