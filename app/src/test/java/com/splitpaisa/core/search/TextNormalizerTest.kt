package com.splitpaisa.core.search

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {
    @Test
    fun normalize_basic() {
        assertEquals("jose", TextNormalizer.normalize("José"))
        assertEquals("hello world", TextNormalizer.normalize("  Hello   World  "))
    }

    @Test
    fun tokenize_splits() {
        val tokens = TextNormalizer.tokenize("Ångström Unit")
        assertEquals(listOf("angstrom", "unit"), tokens)
    }
}
