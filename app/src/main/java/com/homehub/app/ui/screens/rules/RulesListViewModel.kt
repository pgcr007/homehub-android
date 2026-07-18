package com.homehub.app.ui.screens.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.network.ApiClient
import com.homehub.app.network.RuleDto
import com.homehub.app.network.ToggleRuleRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RulesListUiState(
    val rules: List<RuleDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class RulesListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RulesListUiState())
    val uiState: StateFlow<RulesListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = ApiClient.deviceService.listRules()
                _uiState.update { it.copy(rules = response.rules, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "couldn't load rules: ${e.message}") }
            }
        }
    }

    fun toggle(rule: RuleDto, enabled: Boolean) {
        viewModelScope.launch {
            try {
                ApiClient.deviceService.toggleRule(rule._id, ToggleRuleRequest(enabled))
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "couldn't update rule: ${e.message}") }
            }
        }
    }

    fun delete(rule: RuleDto) {
        viewModelScope.launch {
            try {
                ApiClient.deviceService.deleteRule(rule._id)
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "couldn't delete rule: ${e.message}") }
            }
        }
    }
}