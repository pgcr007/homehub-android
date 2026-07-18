package com.homehub.app.realtime

import com.homehub.app.BuildConfig
import com.homehub.app.network.TokenHolder
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

/**
 * Wraps the Socket.IO connection to the HomeHub backend. Auth mirrors the
 * server's expectation from socketService.js: JWT carried in the handshake
 * `auth.token`, same token used for REST calls via TokenHolder.
 *
 * `device:event` arrives in one of two shapes (see mqttSubscriber.js Phase 4
 * update): { deviceId, state: {...} } for capability changes, or
 * { deviceId, status: "..." } for online/offline/unknown transitions.
 */
data class DeviceEvent(
    val deviceId: String,
    val state: Map<String, Any?>? = null,
    val status: String? = null
)

object SocketManager {
    private var socket: Socket? = null

    private val _events = MutableSharedFlow<DeviceEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<DeviceEvent> = _events.asSharedFlow()

    fun connect() {
        if (socket?.connected() == true) return

        val token = TokenHolder.token ?: return
        val opts = IO.Options.builder()
            .setAuth(mapOf("token" to token))
            .setTransports(arrayOf("polling", "websocket"))
            .build()

        val newSocket = IO.socket(BuildConfig.BASE_URL, opts)

        newSocket.on(Socket.EVENT_CONNECT) {
            android.util.Log.d("SocketManager", "connected")
        }
        newSocket.on(Socket.EVENT_DISCONNECT) {
            android.util.Log.d("SocketManager", "disconnected")
        }
        newSocket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            android.util.Log.e("SocketManager", "connect_error: ${args.firstOrNull()}")
        }
        newSocket.on("device:event") { args ->
            val json = args.firstOrNull() as? JSONObject ?: return@on
            val deviceId = json.optString("deviceId", "")
            if (deviceId.isEmpty()) return@on

            val stateObj = json.optJSONObject("state")
            val state = stateObj?.let { jsonObjectToMap(it) }
            val status = if (json.has("status")) json.optString("status") else null

            _events.tryEmit(DeviceEvent(deviceId = deviceId, state = state, status = status))
        }

        newSocket.connect()
        socket = newSocket
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    private fun jsonObjectToMap(json: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        json.keys().forEach { key -> map[key] = json.opt(key) }
        return map
    }
}