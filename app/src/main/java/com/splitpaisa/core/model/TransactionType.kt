package com.splitpaisa.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}
