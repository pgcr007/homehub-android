package com.homehub.app.realtime

import com.homehub.app.BuildConfig
import com.homehub.app.network.HouseholdHolder
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
 * `auth.token` (same token used for REST calls via TokenHolder), PLUS a
 * `householdId` (same active household as HouseholdHolder / the REST
 * X-Household-Id header) — the backend rejects a handshake missing either
 * one. A socket connection is scoped to exactly one household room at a
 * time, matching how the dashboard shows one unit at a time even for a
 * manager who belongs to several.
 *
 * NOTE: prior to Step 4 this only ever sent `token`, so every socket
 * handshake since Phase 6 was rejected with "missing householdId" and live
 * updates were silently broken — the app fell back to whatever the last
 * loadData() REST call showed, no realtime device:event pushes at all.
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

    // Which household the current socket connection is actually joined to
    // (server-side room `household:{householdId}`) — tracked separately
    // from HouseholdHolder.activeHouseholdId so connect() can tell "already
    // connected for the right household, no-op" apart from "connected, but
    // for a household the user just switched away from, reconnect".
    private var connectedHouseholdId: String? = null

    private val _events = MutableSharedFlow<DeviceEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<DeviceEvent> = _events.asSharedFlow()

    /**
     * Connects (or reconnects, if the active household has changed since
     * the last call) for the current TokenHolder/HouseholdHolder state.
     * Safe to call repeatedly — e.g. on every screen resume — since it's a
     * no-op when already connected for the right household.
     */
    fun connect() {
        val token = TokenHolder.token ?: return
        val householdId = HouseholdHolder.activeHouseholdId ?: return

        if (socket?.connected() == true && connectedHouseholdId == householdId) return

        // Either not connected yet, or connected for a *different*
        // household (the user switched active household) — tear down and
        // reconnect fresh rather than trying to reuse the old socket. The
        // backend does support a lighter-weight ack-based `switchHousehold`
        // event for this without a full reconnect, but a clean
        // disconnect+reconnect is simpler and reuses the same well-tested
        // path either way.
        if (socket != null) {
            disconnect()
        }

        val opts = IO.Options.builder()
            .setAuth(mapOf("token" to token, "householdId" to householdId))
            .setTransports(arrayOf("polling", "websocket"))
            .build()

        val newSocket = IO.socket(BuildConfig.BASE_URL, opts)

        newSocket.on(Socket.EVENT_CONNECT) {
            android.util.Log.d("SocketManager", "connected (household=$householdId)")
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
        connectedHouseholdId = householdId
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        connectedHouseholdId = null
    }

    private fun jsonObjectToMap(json: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        json.keys().forEach { key -> map[key] = json.opt(key) }
        return map
    }
}