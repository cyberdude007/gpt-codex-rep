package com.splitpaisa.core.security

import android.content.Context
import com.splitpaisa.core.prefs.SettingsRepository
import kotlinx.coroutines.flow.first

class AppLockManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val pinStorage = PinStorage(context)

    suspend fun isLockRequired(): Boolean {
        val s = settingsRepository.settings.first()
        if (!s.appLockEnabled) return false
        if (s.autoLockMinutes <= 0) return true
        val now = System.currentTimeMillis()
        return now - s.lastLockTimestamp > s.autoLockMinutes * 60 * 1000
    }

    suspend fun recordUnlock() {
        settingsRepository.setLastUnlock(System.currentTimeMillis())
    }

    fun verifyPin(pin: String): Boolean = pinStorage.verifyPin(pin)

    fun savePin(pin: String) { pinStorage.savePin(pin) }
    fun clearPin() { pinStorage.clear() }
}
