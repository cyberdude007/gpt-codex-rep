package com.splitpaisa.data

import com.splitpaisa.core.model.SeedData
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class SeedRepositoryTest {
    @Test
    fun parseSeedData() {
        val text = File("src/main/assets/data/seed/demo_seed.json").readText()
        val seed = Json { ignoreUnknownKeys = true }.decodeFromString(SeedData.serializer(), text)
        assertEquals(3, seed.transactions.size)
        assertEquals("wallet", seed.accounts.first().id)
    }
}
