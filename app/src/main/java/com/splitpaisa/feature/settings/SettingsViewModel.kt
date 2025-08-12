package com.splitpaisa.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitpaisa.core.prefs.Settings
import com.splitpaisa.core.prefs.SettingsRepository
import com.splitpaisa.core.prefs.ThemeMode
import com.splitpaisa.core.prefs.settingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {
    val uiState: StateFlow<Settings> = repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    fun toggleOfflineOnly(value: Boolean) = viewModelScope.launch {
        repo.setOfflineOnly(value)
    }

    fun toggleHideAmounts(value: Boolean) = viewModelScope.launch {
        repo.setHideAmounts(value)
    }

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repo.setTheme(mode) }
    fun setAppLock(enabled: Boolean) = viewModelScope.launch { repo.setAppLock(enabled) }
    fun setAutoLock(mins: Int) = viewModelScope.launch { repo.setAutoLock(mins) }
    fun setBudgetAlerts(p75: Boolean, p100: Boolean) = viewModelScope.launch { repo.setBudgetAlerts(p75, p100) }
    fun setSettleReminder(enabled: Boolean) = viewModelScope.launch { repo.setSettleUpReminder(enabled) }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = SettingsRepository(context.settingsDataStore)
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(repo) as T
            }
        }
    }
}
