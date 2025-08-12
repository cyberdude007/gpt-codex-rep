package com.splitpaisa.core.csv

data class AccountCsv(val id: String, val name: String, val type: String)
data class CategoryCsv(
    val id: String,
    val name: String,
    val kind: String,
    val icon: String?,
    val color: String?,
    val monthlyBudgetPaise: Long?
)
data class TransactionCsv(
    val id: String,
    val type: String,
    val title: String,
    val amountPaise: Long,
    val atEpochMillis: Long,
    val categoryId: String?,
    val accountId: String?,
    val partyId: String?,
    val notes: String?
)
data class PartyCsv(val id: String, val name: String, val createdAt: Long)
data class MemberCsv(val id: String, val partyId: String, val displayName: String, val contact: String?)
data class SplitCsv(val id: String, val transactionId: String, val memberId: String, val sharePaise: Long)
data class SettlementCsv(
    val id: String,
    val partyId: String,
    val payerId: String,
    val payeeId: String,
    val amountPaise: Long,
    val methodNote: String?,
    val atEpochMillis: Long,
    val memo: String?
)
