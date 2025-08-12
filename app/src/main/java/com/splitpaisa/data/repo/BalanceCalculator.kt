package com.splitpaisa.data.repo

import com.splitpaisa.core.model.Settlement
import com.splitpaisa.core.model.Split
import com.splitpaisa.core.model.Transaction

/** Utility to compute net balances for party members. */
object BalanceCalculator {
    const val YOU_ID = "you"

    fun calculate(
        transactions: List<Transaction>,
        splitsByTx: Map<String, List<Split>>,
        settlements: List<Settlement>,
        youId: String = YOU_ID
    ): Balances {
        val map = mutableMapOf<String, Long>()

        transactions.forEach { t ->
            val payer = t.payerId ?: youId
            map[payer] = map.getOrDefault(payer, 0L) + t.amountPaise
            splitsByTx[t.id].orEmpty().forEach { s ->
                map[s.memberId] = map.getOrDefault(s.memberId, 0L) - s.sharePaise
            }
        }

        settlements.forEach { s ->
            map[s.payerId] = map.getOrDefault(s.payerId, 0L) + s.amountPaise
            map[s.payeeId] = map.getOrDefault(s.payeeId, 0L) - s.amountPaise
        }

        return Balances(map, map[youId] ?: 0L)
    }
}
