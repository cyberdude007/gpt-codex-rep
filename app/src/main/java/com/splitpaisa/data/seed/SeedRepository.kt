package com.splitpaisa.data.seed

import android.content.Context
import com.splitpaisa.core.model.SeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class SeedRepository(private val context: Context) {
    private var cache: SeedData? = null

    suspend fun getSeedData(): SeedData {
        return cache ?: loadSeed().also { cache = it }
    }

    private suspend fun loadSeed(): SeedData = withContext(Dispatchers.IO) {
        val json = context.assets.open("data/seed/demo_seed.json")
            .bufferedReader()
            .use { it.readText() }
        Json { ignoreUnknownKeys = true }.decodeFromString(SeedData.serializer(), json)
    }
}
