package com.splitpaisa.core.search

import java.text.Normalizer

object TextNormalizer {
    fun normalize(input: String): String {
        val lower = input.lowercase()
        val normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "")
            .replace("[^a-z0-9\\s]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
        return normalized
    }

    fun tokenize(input: String): List<String> {
        val norm = normalize(input)
        return if (norm.isBlank()) emptyList() else norm.split(' ')
    }
}
