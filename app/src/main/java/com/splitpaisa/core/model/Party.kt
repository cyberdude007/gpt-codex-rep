package com.splitpaisa.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Party(
    val id: String,
    val name: String,
    val members: List<Member>
)

@Serializable
data class Member(
    val id: String,
    val name: String
)
