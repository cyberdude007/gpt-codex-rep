package com.splitpaisa.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Settlement(
    val id: String,
    val partyId: String,
    val payerId: String,
    val payeeId: String,
    val amountPaise: Long,
    val methodNote: String,
    val atEpochMillis: Long,
    val memo: String? = null
)
