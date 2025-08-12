package com.splitpaisa.util

import com.splitpaisa.core.util.SplitMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitRoundingTest {
    @Test
    fun equalSplitRounding() {
        for (n in 3..7) {
            val total = 1001L
            val members = (1..n).map { "m$it" }
            val result = SplitMath.equalSplit(total, members)
            assertEquals(total, result.shares.values.sum())
            val values = result.shares.values
            assertTrue(values.maxOrNull()!! - values.minOrNull()!! <= 1)
        }
    }

    @Test
    fun percentTrickyTotals() {
        val total = 1001L
        val percents = mapOf("a" to 30.0, "b" to 30.0, "c" to 40.0)
        val result = SplitMath.percentageSplit(total, percents)
        assertEquals(total, result.shares.values.sum())
        assertTrue(result.adjusted)
        assertEquals(301L, result.shares["a"])
    }

    @Test
    fun itemizedDrift() {
        val total = 1001L
        val items = mapOf("a" to 500L, "b" to 500L)
        val result = SplitMath.itemizedSplit(total, items)
        assertEquals(total, result.shares.values.sum())
        assertTrue(result.adjusted)
        assertEquals(501L, result.shares["a"])
    }
}
