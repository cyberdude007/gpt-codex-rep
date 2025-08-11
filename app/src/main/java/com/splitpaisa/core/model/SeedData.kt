package com.splitpaisa.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SeedData(
    val accounts: List<Account>,
    val categories: List<Category>,
    val party: SeedParty,
    val transactions: List<Transaction>
)

@Serializable
data class SeedParty(
    val id: String,
    val name: String,
    val members: List<SeedMember>
)

@Serializable
data class SeedMember(
    val id: String,
    val name: String
)
