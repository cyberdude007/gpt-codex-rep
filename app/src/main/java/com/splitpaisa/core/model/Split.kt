package com.splitpaisa.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Split(
    val id: String,
    val transactionId: String,
    val memberId: String,
    val sharePaise: Long
)
