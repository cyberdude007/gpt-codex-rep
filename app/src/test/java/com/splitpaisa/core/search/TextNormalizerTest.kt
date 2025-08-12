package com.splitpaisa.core.search

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {
    @Test
    fun normalizeRemovesDiacritics() {
        assertEquals("cafe", TextNormalizer.normalize("Café"))
    }

    @Test
    fun tokenizeSplits() {
        assertEquals(listOf("hello", "world"), TextNormalizer.tokenize("  Hello, World!  "))
    }
}
