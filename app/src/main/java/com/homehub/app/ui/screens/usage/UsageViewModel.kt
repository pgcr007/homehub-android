package com.homehub.app.ui.screens.usage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.network.ApiClient
import com.homehub.app.network.UsageDeviceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsageUiState(
    val devices: List<UsageDeviceDto> = emptyList(),
    val totalOnHours: Double = 0.0,
    val windowDays: Int = 7,
    val isLoading: Boolean = true,
    val error: String? = null
)

class UsageViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UsageUiState())
    val uiState: StateFlow<UsageUiState> = _uiState.asStateFlow()

    init {
        load(7)
    }

    fun load(days: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = ApiClient.deviceService.getUsage(days)
                _uiState.update {
                    it.copy(
                        devices = response.devices,
                        totalOnHours = response.totalOnHours,
                        windowDays = response.windowDays,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "couldn't load usage: ${e.message}") }
            }
        }
    }
}