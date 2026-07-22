package com.homehub.app.ui.screens.household

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.network.AddMemberRequest
import com.homehub.app.network.ApiClient
import com.homehub.app.network.HouseholdDetailDto
import com.homehub.app.network.HouseholdMemberDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MembersUiState(
    val isLoading: Boolean = true,
    val household: HouseholdDetailDto? = null,
    // "owner" | "manager" | "member" for the signed-in user in THIS
    // household (from GET /api/households/current's top-level myRole) —
    // drives whether the add/remove-member controls show at all.
    val myRole: String? = null,
    val isInviting: Boolean = false,
    val error: String? = null
)

class MembersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MembersUiState())
    val uiState: StateFlow<MembersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = ApiClient.householdService.getCurrentHousehold()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    household = response.household,
                    myRole = response.myRole
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Couldn't load members: ${e.message ?: "unknown error"}"
                )
            }
        }
    }

    // "Invite" in the UI sense — the backend adds an existing account by
    // email (see householdController.addMember's own comment: no
    // invite-email flow yet), it doesn't send anything to that address.
    fun addMember(email: String, role: String) {
        if (email.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isInviting = true, error = null)
            try {
                val response = ApiClient.householdService.addMember(AddMemberRequest(email.trim(), role))
                _uiState.value = _uiState.value.copy(isInviting = false, household = response.household)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isInviting = false,
                    error = "Couldn't add member: ${e.message ?: "unknown error"}"
                )
            }
        }
    }

    fun removeMember(member: HouseholdMemberDto) {
        viewModelScope.launch {
            try {
                ApiClient.householdService.removeMember(member.user._id)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Couldn't remove member: ${e.message ?: "unknown error"}"
                )
            }
        }
    }
}