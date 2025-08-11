package com.splitpaisa.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val type: TransactionType,
    val title: String,
    val amountPaise: Long,
    val atEpochMillis: Long,
    val categoryId: String,
    val accountId: String,
    val partyId: String? = null
)
