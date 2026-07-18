package com.homehub.app.ui.screens.adddevice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.network.ApiClient
import com.homehub.app.network.CreateDeviceRequest
import com.homehub.app.network.CreateDeviceResponse
import com.homehub.app.network.CreateRoomRequest
import com.homehub.app.network.DEVICE_TYPE_OPTIONS
import com.homehub.app.network.DeviceTypeOption
import com.homehub.app.network.RoomDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddDeviceUiState(
    val rooms: List<RoomDto> = emptyList(),
    val name: String = "",
    val selectedType: DeviceTypeOption = DEVICE_TYPE_OPTIONS.first(),
    val identifier: String = "",
    val selectedRoomId: String? = null, // null = unassigned
    val isCreatingRoom: Boolean = false,
    val newRoomName: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    // Set only once the POST succeeds. Non-null drives the confirmation
    // card instead of the form — for webhook devices this is the one and
    // only screen where the secret is shown as part of the creation flow.
    val result: CreateDeviceResponse? = null
)

class AddDeviceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AddDeviceUiState())
    val uiState: StateFlow<AddDeviceUiState> = _uiState.asStateFlow()

    init {
        loadRooms()
    }

    private fun loadRooms() {
        viewModelScope.launch {
            try {
                val response = ApiClient.deviceService.listRooms()
                _uiState.update { it.copy(rooms = response.rooms) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "couldn't load rooms: ${e.message}") }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onTypeChange(type: DeviceTypeOption) = _uiState.update { it.copy(selectedType = type) }
    fun onIdentifierChange(value: String) = _uiState.update { it.copy(identifier = value, error = null) }
    fun onRoomSelected(roomId: String?) = _uiState.update { it.copy(selectedRoomId = roomId) }
    fun onNewRoomNameChange(value: String) = _uiState.update { it.copy(newRoomName = value) }

    fun toggleCreatingRoom(show: Boolean) {
        _uiState.update { it.copy(isCreatingRoom = show, newRoomName = "") }
    }

    fun createRoom() {
        val name = _uiState.value.newRoomName.trim()
        if (name.isEmpty()) return

        viewModelScope.launch {
            try {
                val response = ApiClient.deviceService.createRoom(CreateRoomRequest(name))
                _uiState.update {
                    it.copy(
                        rooms = it.rooms + response.room,
                        selectedRoomId = response.room._id,
                        isCreatingRoom = false,
                        newRoomName = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "couldn't create room: ${e.message}") }
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "name is required") }
            return
        }
        if (state.identifier.isBlank()) {
            _uiState.update { it.copy(error = "identifier is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val response = ApiClient.deviceService.createDevice(
                    CreateDeviceRequest(
                        name = state.name.trim(),
                        type = state.selectedType.value,
                        identifier = state.identifier.trim(),
                        room = state.selectedRoomId
                    )
                )
                _uiState.update { it.copy(isSubmitting = false, result = response) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, error = "couldn't add device: ${e.message}")
                }
            }
        }
    }
}