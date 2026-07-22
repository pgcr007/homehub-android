package com.homehub.app.ui.screens.household

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.network.ApiClient
import com.homehub.app.network.CreateHouseholdRequest
import com.homehub.app.network.HouseholdDto
import com.homehub.app.network.HouseholdHolder
import com.homehub.app.network.applyActiveHousehold
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HouseholdSwitcherUiState(
    val households: List<HouseholdDto> = emptyList(),
    val activeHouseholdId: String? = null,
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val errorMessage: String? = null
)

class HouseholdSwitcherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HouseholdSwitcherUiState())
    val uiState: StateFlow<HouseholdSwitcherUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val households = ApiClient.householdService.listMyHouseholds().households
                _uiState.value = _uiState.value.copy(
                    households = households,
                    activeHouseholdId = HouseholdHolder.activeHouseholdId,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Couldn't load households: ${e.message ?: "unknown error"}"
                )
            }
        }
    }

    /**
     * Switching is a local, no-network operation — the household list
     * (with this user's role on each one) was already fetched above.
     */
    fun select(household: HouseholdDto) {
        applyActiveHousehold(household)
        _uiState.value = _uiState.value.copy(activeHouseholdId = household._id)
    }

    fun createHousehold(name: String, type: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null)
            try {
                val created = ApiClient.householdService.createHousehold(
                    CreateHouseholdRequest(name = name.trim(), type = type)
                )
                // The create endpoint doesn't return myRole (only
                // listMyHouseholds does), but the creator is always made
                // 'owner' server-side (householdController.createHousehold),
                // so it's safe to set it directly rather than a second round trip.
                val ownedHousehold = created.household.copy(myRole = "owner")
                applyActiveHousehold(ownedHousehold)
                _uiState.value = _uiState.value.copy(
                    households = _uiState.value.households + ownedHousehold,
                    activeHouseholdId = ownedHousehold._id,
                    isCreating = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    errorMessage = "Couldn't create household: ${e.message ?: "unknown error"}"
                )
            }
        }
    }
}