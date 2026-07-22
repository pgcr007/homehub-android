package com.homehub.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.network.ApiClient
import com.homehub.app.network.DeviceDto
import com.homehub.app.network.RoomDto
import com.homehub.app.realtime.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val rooms: List<RoomDto> = emptyList(),
    val devices: List<DeviceDto> = emptyList()
)

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
        // The SharedFlow subscription is started exactly once, for the
        // ViewModel's whole lifetime — SocketManager.connect() itself is
        // safe to call again later (see refreshForPossibleHouseholdChange),
        // but re-launching this collect() on every resume would create a
        // new duplicate collector each time.
        viewModelScope.launch {
            SocketManager.events.collect { event ->
                _uiState.update { current ->
                    val updatedDevices = current.devices.map { device ->
                        if (device._id != event.deviceId) return@map device
                        device.copy(
                            state = event.state?.let { device.state + it } ?: device.state,
                            status = event.status ?: device.status
                        )
                    }
                    current.copy(devices = updatedDevices)
                }
            }
        }
        SocketManager.connect()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val roomsResponse = ApiClient.deviceService.listRooms()
                val devicesResponse = ApiClient.deviceService.listDevices()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rooms = roomsResponse.rooms,
                        devices = devicesResponse.devices
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "failed to load dashboard") }
            }
        }
    }

    /**
     * Call on every screen resume, not just after returning from the
     * household switcher — cheap either way, since both loadData() and
     * SocketManager.connect() are no-ops/idempotent when nothing actually
     * changed. Reloads REST data for whatever the active household
     * currently is, and reconnects the socket if it changed underneath us.
     */
    fun refreshForPossibleHouseholdChange() {
        loadData()
        SocketManager.connect()
    }

    fun sendCommand(device: DeviceDto, command: Map<String, Any>) {
        viewModelScope.launch {
            try {
                ApiClient.deviceService.sendCommand(device._id, command)
                // No optimistic update — wait for the real device:event to
                // confirm the change actually took effect, since a command
                // being *sent* isn't the same claim as the device *applying* it.
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "command failed: ${e.message}") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.disconnect()
    }
}