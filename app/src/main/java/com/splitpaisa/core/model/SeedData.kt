package com.splitpaisa.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SeedData(
    val accounts: List<Account>,
    val categories: List<Category>,
    val party: Party,
    val transactions: List<Transaction>
)
