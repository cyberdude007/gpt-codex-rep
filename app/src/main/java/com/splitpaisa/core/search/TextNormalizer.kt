package com.splitpaisa.core.search

object TextNormalizer {
    private val spaceRegex = "\\s+".toRegex()
    private val diacriticsRegex = "\\p{Mn}+".toRegex()

    fun normalize(input: String): String {
        val lower = input.lowercase()
        val normalized = java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
        val noDiacritics = diacriticsRegex.replace(normalized, "")
        return spaceRegex.replace(noDiacritics, " ").trim()
    }

    fun tokenize(input: String): List<String> =
        normalize(input).split(' ').filter { it.isNotEmpty() }
}
