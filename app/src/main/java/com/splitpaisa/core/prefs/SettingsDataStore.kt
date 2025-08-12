package com.splitpaisa.core.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import java.io.InputStream
import java.io.OutputStream

@Serializable
enum class ThemeMode {
    @ProtoNumber(0) SYSTEM,
    @ProtoNumber(1) LIGHT,
    @ProtoNumber(2) DARK
}

@Serializable
data class Settings(
    @ProtoNumber(1) val themeMode: ThemeMode = ThemeMode.SYSTEM,
    @ProtoNumber(2) val currencyCode: String = "INR",
    @ProtoNumber(3) val dateFormat: String = "dd-MM-yyyy",
    @ProtoNumber(4) val hideAmounts: Boolean = false,
    @ProtoNumber(5) val appLockEnabled: Boolean = false,
    @ProtoNumber(6) val autoLockMinutes: Int = 0,
    @ProtoNumber(7) val lastLockTimestamp: Long = 0L,
    @ProtoNumber(8) val budgetAlert75: Boolean = false,
    @ProtoNumber(9) val budgetAlert100: Boolean = false,
    @ProtoNumber(10) val settleUpReminder: Boolean = false,
    @ProtoNumber(11) val offlineOnly: Boolean = true
)

object SettingsSerializer : Serializer<Settings> {
    override val defaultValue: Settings = Settings()

    override suspend fun readFrom(input: InputStream): Settings = try {
        ProtoBuf.decodeFromByteArray(Settings.serializer(), input.readBytes())
    } catch (e: Exception) {
        Settings()
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(Settings.serializer(), t))
    }
}

val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = "settings.pb",
    serializer = SettingsSerializer
)
