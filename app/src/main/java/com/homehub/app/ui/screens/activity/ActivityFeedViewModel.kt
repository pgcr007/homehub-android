package com.homehub.app.ui.screens.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.network.ApiClient
import com.homehub.app.network.DeviceDto
import com.homehub.app.network.EventDto
import com.homehub.app.realtime.DeviceEvent
import com.homehub.app.realtime.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ActivityItem(
    val id: String,
    val deviceName: String,
    val description: String,
    val sourceLabel: String,
    val timestamp: String
)

data class ActivityFeedUiState(
    val items: List<ActivityItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ActivityFeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityFeedUiState())
    val uiState: StateFlow<ActivityFeedUiState> = _uiState.asStateFlow()

    // Needed to label live socket events, which only carry a deviceId.
    private var deviceLookup: Map<String, DeviceDto> = emptyMap()

    private val timeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")

    init {
        loadHistory()
        observeLiveEvents()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val devices = ApiClient.deviceService.listDevices().devices
                deviceLookup = devices.associateBy { it._id }

                val response = ApiClient.deviceService.listEvents(limit = 50)
                val items = response.events.mapNotNull(::toHistoryItem)
                _uiState.update { it.copy(items = items, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "couldn't load activity: ${e.message}")
                }
            }
        }
    }

    private fun observeLiveEvents() {
        viewModelScope.launch {
            SocketManager.events.collect { event ->
                val device = deviceLookup[event.deviceId] ?: return@collect
                val item = ActivityItem(
                    id = "live-${System.currentTimeMillis()}-${event.deviceId}",
                    deviceName = device.name,
                    description = describeLiveEvent(event),
                    sourceLabel = "live",
                    timestamp = "just now"
                )
                // Prepend rather than reload — this is the real-time half of
                // the feed; loadHistory() covers what happened before this
                // screen was opened.
                _uiState.update { it.copy(items = listOf(item) + it.items) }
            }
        }
    }

    private fun toHistoryItem(event: EventDto): ActivityItem? {
        val device = event.device ?: return null
        return ActivityItem(
            id = event._id,
            deviceName = device.name,
            description = describeHistoryEvent(event),
            sourceLabel = event.source,
            timestamp = formatTimestamp(event.createdAt)
        )
    }

    private fun describeHistoryEvent(event: EventDto): String = when (event.type) {
        "online" -> "Came online"
        "offline" -> "Went offline"
        "unknown" -> "Status unknown (stale)"
        "rule_fired" -> "Rule triggered"
        else -> describeState(event.normalizedState)
    }

    private fun describeLiveEvent(event: DeviceEvent): String = when {
        event.status != null -> when (event.status) {
            "online" -> "Came online"
            "offline" -> "Went offline"
            else -> "Status: ${event.status}"
        }
        event.state != null -> describeState(event.state)
        else -> "Updated"
    }

    private fun describeState(state: Map<String, Any?>): String {
        val parts = mutableListOf<String>()
        state["power"]?.let { parts.add("power $it") }
        state["brightness"]?.let { parts.add("brightness $it%") }
        state["contact"]?.let { parts.add("contact $it") }
        state["motion"]?.let { parts.add("motion $it") }
        state["temperature"]?.let { parts.add("temp $it°") }
        return if (parts.isEmpty()) "State updated" else parts.joinToString(", ")
    }

    private fun formatTimestamp(iso: String): String = try {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).format(timeFormatter)
    } catch (e: Exception) {
        iso
    }
}