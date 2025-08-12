package com.splitpaisa.core.prefs

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class SettingsRepository(private val dataStore: DataStore<Settings>) {
    val settings: Flow<Settings> = dataStore.data

    suspend fun setTheme(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    suspend fun setHideAmounts(hide: Boolean) = update { it.copy(hideAmounts = hide) }
    suspend fun setOfflineOnly(offline: Boolean) = update { it.copy(offlineOnly = offline) }
    suspend fun setAppLock(enabled: Boolean) = update { it.copy(appLockEnabled = enabled) }
    suspend fun setAutoLock(minutes: Int) = update { it.copy(autoLockMinutes = minutes) }
    suspend fun setLastUnlock(timestamp: Long) = update { it.copy(lastLockTimestamp = timestamp) }
    suspend fun setBudgetAlerts(p75: Boolean, p100: Boolean) = update { it.copy(budgetAlert75 = p75, budgetAlert100 = p100) }
    suspend fun setSettleUpReminder(enabled: Boolean) = update { it.copy(settleUpReminder = enabled) }

    private suspend fun update(block: (Settings) -> Settings) {
        dataStore.updateData { current -> block(current) }
    }
}
