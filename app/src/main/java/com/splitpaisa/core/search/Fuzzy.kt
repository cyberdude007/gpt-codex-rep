package com.splitpaisa.core.search

object Fuzzy {
    fun rank(query: String, candidate: String): Int {
        val qTokens = TextNormalizer.tokenize(query)
        val cTokens = TextNormalizer.tokenize(candidate)
        return rankTokens(qTokens, cTokens)
    }

    fun rankTokens(queryTokens: List<String>, candidateTokens: List<String>): Int {
        val candidate = candidateTokens.joinToString(" ")
        val query = queryTokens.joinToString(" ")
        if (candidate == query) return 0
        var score = 0
        score += when {
            candidate.startsWith(query) -> 1
            candidate.contains(query) -> 2
            else -> 3
        }
        val overlap = queryTokens.count { qt -> candidateTokens.any { it.startsWith(qt) } }
        score += (queryTokens.size - overlap)
        score += editDistance(query, candidate.take(query.length))
        return score
    }

    private fun editDistance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[a.length][b.length]
    }
}
