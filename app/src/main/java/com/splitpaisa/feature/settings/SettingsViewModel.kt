package com.splitpaisa.feature.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingsUiState(
    val offlineOnly: Boolean = true,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val hideAmounts: Boolean = false
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun toggleOfflineOnly(value: Boolean) {
        _uiState.value = _uiState.value.copy(offlineOnly = value)
    }

    fun toggleHideAmounts(value: Boolean) {
        _uiState.value = _uiState.value.copy(hideAmounts = value)
    }

    fun setTheme(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(theme = mode)
    }
}
