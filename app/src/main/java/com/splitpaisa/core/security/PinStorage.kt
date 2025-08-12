package com.splitpaisa.core.security

import android.content.Context
import android.util.Base64

class PinStorage(context: Context) {
    private val prefs = EncryptedPrefs(context)

    fun savePin(pin: String) {
        val salt = PinHasher.randomSalt()
        val hash = PinHasher.hash(pin, salt)
        prefs.putString(KEY_SALT, Base64.encodeToString(salt, Base64.DEFAULT))
        prefs.putString(KEY_HASH, hash)
    }

    fun verifyPin(pin: String): Boolean {
        val saltB64 = prefs.getString(KEY_SALT) ?: return false
        val hashStored = prefs.getString(KEY_HASH) ?: return false
        val salt = Base64.decode(saltB64, Base64.DEFAULT)
        val hash = PinHasher.hash(pin, salt)
        return hashStored == hash
    }

    fun clear() {
        prefs.remove(KEY_SALT)
        prefs.remove(KEY_HASH)
    }

    companion object {
        private const val KEY_SALT = "pin_salt"
        private const val KEY_HASH = "pin_hash"
    }
}
