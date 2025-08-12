package com.splitpaisa.core.search

import org.junit.Assert.assertTrue
import org.junit.Test

class FuzzyRankTest {
    @Test
    fun exactBeatsPrefix() {
        val q = "meera"
        val exact = Fuzzy.rank(q, "Meera")
        val prefix = Fuzzy.rank(q, "Meerabai")
        assertTrue(exact < prefix)
    }

    @Test
    fun editDistanceMatters() {
        val q = "mr"
        val meera = Fuzzy.rank(q, "Meera")
        val amar = Fuzzy.rank(q, "Amar")
        assertTrue(meera < amar)
    }
}
