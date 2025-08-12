package com.splitpaisa.core.search

object Fuzzy {
    fun rank(query: String, candidate: String): Int {
        val qTokens = TextNormalizer.tokenize(query)
        val cTokens = TextNormalizer.tokenize(candidate)
        return rankTokens(qTokens, cTokens)
    }

    fun rankTokens(qTokens: List<String>, cTokens: List<String>): Int {
        if (qTokens.isEmpty() || cTokens.isEmpty()) return 0
        val query = qTokens.joinToString(" ")
        val candidate = cTokens.joinToString(" ")
        var score = 0
        if (candidate == query) score += 1000
        if (candidate.startsWith(query)) score += 500
        if (candidate.contains(query)) score += 100
        // token overlap and order
        var lastIndex = -1
        var overlap = 0
        for (q in qTokens) {
            val idx = cTokens.indexOfFirst { it.startsWith(q) }
            if (idx >= 0) {
                overlap++
                if (lastIndex >= 0 && idx == lastIndex + 1) score += 20
                if (idx == overlap - 1) score += 10
                lastIndex = idx
            } else {
                // small edit distance bonus
                cTokens.forEach { token ->
                    val dist = editDistance(q, token)
                    when (dist) {
                        1 -> score += 5
                        2 -> score += 2
                        3 -> score += 2
                    }
                }
            }
        }
        score += overlap * 50
        // starts with bonus
        if (cTokens.first().startsWith(qTokens.first())) score += 30
        // shorter candidate preferred
        score -= candidate.length
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
