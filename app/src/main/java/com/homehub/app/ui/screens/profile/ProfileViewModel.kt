package com.homehub.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.network.ApiClient
import com.homehub.app.network.ChangePasswordRequest
import com.homehub.app.network.HouseholdHolder
import com.homehub.app.network.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserDto? = null,
    val error: String? = null,
    // Change-password sub-flow, kept in the same state rather than a
    // separate screen/ViewModel — it's a small dialog, not worth the
    // navigation/lifecycle overhead of its own destination.
    val isChangingPassword: Boolean = false,
    val passwordChangeError: String? = null,
    val passwordChangeSuccess: Boolean = false
)

/**
 * Profile screen (post-Phase 7, last item on the original feature-ideas
 * list). Deferred at Phase 7 because there was nothing to show — Register
 * existing now means there's a real name/email/created-at to display.
 *
 * Deliberately thin: this is a read-mostly screen (name, email, member
 * since, active household) plus one write action (change password).
 * Household switching itself isn't duplicated here — it's a shortcut to
 * the existing HouseholdSwitcherScreen, not a second implementation.
 */
class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = ApiClient.authService.me()
                _uiState.update { it.copy(isLoading = false, user = response.user) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "couldn't load profile") }
            }
        }
    }

    fun activeHouseholdName(): String? = HouseholdHolder.activeHouseholdName
    fun activeHouseholdRole(): String? = HouseholdHolder.activeHouseholdRole

    fun openChangePassword() = _uiState.update {
        it.copy(isChangingPassword = true, passwordChangeError = null, passwordChangeSuccess = false)
    }

    fun dismissChangePassword() = _uiState.update {
        it.copy(isChangingPassword = false, passwordChangeError = null)
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (newPassword.length < 8) {
            _uiState.update { it.copy(passwordChangeError = "New password must be at least 8 characters") }
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(passwordChangeError = "New passwords don't match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(passwordChangeError = null) }
            try {
                ApiClient.authService.changePassword(ChangePasswordRequest(currentPassword, newPassword))
                _uiState.update {
                    it.copy(isChangingPassword = false, passwordChangeSuccess = true, passwordChangeError = null)
                }
            } catch (e: Exception) {
                // The backend returns 401 with "current password is incorrect"
                // for a wrong current password, and 400 for a too-short new
                // one (also guarded client-side above) — e.message from
                // Retrofit/OkHttp on a non-2xx is generally just the raw
                // HTTP status line, not the JSON body, so this is a
                // reasonable fallback rather than the exact server message.
                _uiState.update { it.copy(passwordChangeError = "Couldn't change password: ${e.message}") }
            }
        }
    }
}