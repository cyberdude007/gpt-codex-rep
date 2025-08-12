package com.splitpaisa.core.search

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class FuzzyRankTest {
    @Test
    fun exact_prefix_substring() {
        val exact = Fuzzy.rank("meera", "Meera")
        val prefix = Fuzzy.rank("mee", "Meera")
        val substr = Fuzzy.rank("eer", "Meera")
        assertTrue(exact > prefix)
        assertTrue(prefix > substr)
    }

    @Test
    fun edit_distance_preference() {
        val mr = Fuzzy.rank("mr", "Meera")
        val rav = Fuzzy.rank("rav", "Ravi")
        assertTrue(mr > Fuzzy.rank("mr", "Ravi"))
        assertTrue(rav > Fuzzy.rank("rav", "Meera"))
    }

    @Test
    fun deterministic_tieBreaker() {
        val list = listOf("Alan", "Al")
        val sorted = list.sortedWith(
            compareByDescending<String> { Fuzzy.rank("al", it) }
                .thenBy { it.length }
                .thenBy { it }
        )
        assertEquals("Al", sorted.first())
    }
}
