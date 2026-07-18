package com.homehub.app.ui.screens.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.network.ApiClient
import com.homehub.app.network.CreateActionRequest
import com.homehub.app.network.CreateClauseRequest
import com.homehub.app.network.CreateRuleRequest
import com.homehub.app.network.DeviceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClauseForm(
    val deviceId: String? = null,
    val capability: String = "",
    val operator: String = "eq",
    val value: String = ""
)

data class ActionForm(
    val type: String = "device_command", // "device_command" | "notify"
    val deviceId: String? = null,
    val capability: String = "",
    val value: String = "",
    val message: String = ""
)

data class CreateRuleUiState(
    val devices: List<DeviceDto> = emptyList(),
    val name: String = "",
    val trigger: ClauseForm = ClauseForm(),
    val conditions: List<ClauseForm> = emptyList(),
    val actions: List<ActionForm> = listOf(ActionForm()),
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val created: Boolean = false,
    val warnings: List<String>? = null
)

class CreateRuleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRuleUiState())
    val uiState: StateFlow<CreateRuleUiState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    private fun loadDevices() {
        viewModelScope.launch {
            try {
                val response = ApiClient.deviceService.listDevices()
                _uiState.update { it.copy(devices = response.devices) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "couldn't load devices: ${e.message}") }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }

    fun updateTrigger(form: ClauseForm) = _uiState.update { it.copy(trigger = form) }

    fun addCondition() = _uiState.update { it.copy(conditions = it.conditions + ClauseForm()) }
    fun removeCondition(index: Int) = _uiState.update {
        it.copy(conditions = it.conditions.toMutableList().apply { removeAt(index) })
    }
    fun updateCondition(index: Int, form: ClauseForm) = _uiState.update {
        it.copy(conditions = it.conditions.toMutableList().apply { set(index, form) })
    }

    fun addAction() = _uiState.update { it.copy(actions = it.actions + ActionForm()) }
    fun removeAction(index: Int) = _uiState.update {
        it.copy(actions = it.actions.toMutableList().apply { removeAt(index) })
    }
    fun updateAction(index: Int, form: ActionForm) = _uiState.update {
        it.copy(actions = it.actions.toMutableList().apply { set(index, form) })
    }

    private fun setError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    private fun buildClauseRequest(form: ClauseForm, label: String): CreateClauseRequest? {
        val deviceId = form.deviceId
        if (deviceId == null) {
            setError("select a device for $label")
            return null
        }
        if (form.capability.isBlank()) {
            setError("select a capability for $label")
            return null
        }
        if (form.operator != "changed" && form.value.isBlank()) {
            setError("enter a value for $label (or use the 'changed' operator)")
            return null
        }
        return CreateClauseRequest(
            device = deviceId,
            capability = form.capability,
            operator = form.operator,
            value = if (form.operator == "changed") null else form.value
        )
    }

    private fun buildActionRequest(form: ActionForm, index: Int): CreateActionRequest? {
        return if (form.type == "device_command") {
            val deviceId = form.deviceId
            if (deviceId == null) {
                setError("select a device for action ${index + 1}")
                return null
            }
            if (form.capability.isBlank()) {
                setError("select a capability for action ${index + 1}")
                return null
            }
            if (form.value.isBlank()) {
                setError("enter a value for action ${index + 1}")
                return null
            }
            CreateActionRequest(
                type = "device_command",
                device = deviceId,
                capability = form.capability,
                value = form.value
            )
        } else {
            if (form.message.isBlank()) {
                setError("enter a message for action ${index + 1}")
                return null
            }
            CreateActionRequest(type = "notify", message = form.message.trim())
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            setError("name is required")
            return
        }

        val trigger = buildClauseRequest(state.trigger, "the trigger") ?: return

        val conditions = mutableListOf<CreateClauseRequest>()
        for (c in state.conditions) {
            conditions.add(buildClauseRequest(c, "a condition") ?: return)
        }

        if (state.actions.isEmpty()) {
            setError("at least one action is required")
            return
        }
        val actions = mutableListOf<CreateActionRequest>()
        for ((index, a) in state.actions.withIndex()) {
            actions.add(buildActionRequest(a, index) ?: return)
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val response = ApiClient.deviceService.createRule(
                    CreateRuleRequest(
                        name = state.name.trim(),
                        trigger = trigger,
                        conditions = conditions,
                        actions = actions
                    )
                )
                _uiState.update {
                    it.copy(isSubmitting = false, created = true, warnings = response.warnings)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, error = "couldn't create rule: ${e.message}")
                }
            }
        }
    }
}