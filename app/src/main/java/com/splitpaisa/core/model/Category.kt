package com.splitpaisa.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val kind: TransactionType,
    val icon: String,
    val color: String
)
