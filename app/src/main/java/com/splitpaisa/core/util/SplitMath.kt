package com.splitpaisa.core.util

import kotlin.math.roundToLong

object SplitMath {
    data class Result(val shares: Map<String, Long>, val adjusted: Boolean)

    fun equalSplit(total: Long, memberIds: List<String>): Result {
        val base = total / memberIds.size
        val rem = total % memberIds.size
        val sorted = memberIds.sorted()
        val map = mutableMapOf<String, Long>()
        sorted.forEachIndexed { index, id ->
            map[id] = base + if (index < rem) 1 else 0
        }
        return Result(map, rem != 0L)
    }

    fun unequalSplit(total: Long, shares: Map<String, Long>): Result {
        val normalized = normalize(shares, total)
        val adjusted = shares.values.sum() != total
        return Result(normalized, adjusted)
    }

    fun percentageSplit(total: Long, percentages: Map<String, Double>): Result {
        val raw = percentages.mapValues { (_, pct) ->
            ((total * pct) / 100.0).roundToLong()
        }
        val normalized = normalize(raw, total)
        val adjusted = normalized.values.sum() != raw.values.sum()
        return Result(normalized, adjusted)
    }

    fun itemizedSplit(total: Long, items: Map<String, Long>): Result {
        val normalized = normalize(items, total)
        val adjusted = items.values.sum() != total
        return Result(normalized, adjusted)
    }

    private fun normalize(shares: Map<String, Long>, total: Long): Map<String, Long> {
        val sum = shares.values.sum()
        var diff = total - sum
        if (diff == 0L) return shares
        val result = shares.toMutableMap()
        val sorted = shares.keys.sorted()
        val step = if (diff > 0) 1 else -1
        for (id in sorted) {
            if (diff == 0L) break
            result[id] = result.getValue(id) + step
            diff -= step
        }
        return result
    }
}
