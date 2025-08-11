package com.splitpaisa.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val type: TransactionType,
    val title: String,
    val amountPaise: Long,
    val atEpochMillis: Long,
    val categoryId: String? = null,
    val accountId: String? = null,
    val partyId: String? = null,
    val notes: String? = null,
    val receiptUri: String? = null,
    val recurringMeta: String? = null
)
