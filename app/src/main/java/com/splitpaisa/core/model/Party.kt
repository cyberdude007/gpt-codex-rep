package com.splitpaisa.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Party(
    val id: String,
    val name: String,
    val createdAt: Long
)

@Serializable
data class PartyMember(
    val id: String,
    val partyId: String,
    val displayName: String,
    val contact: String? = null
)
