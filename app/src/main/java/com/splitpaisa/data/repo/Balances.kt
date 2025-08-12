package com.splitpaisa.data.repo

data class Balances(
    val perMember: Map<String, Long>,
    val you: Long
)
