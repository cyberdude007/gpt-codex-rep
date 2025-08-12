package com.splitpaisa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.splitpaisa.core.model.Transaction
import com.splitpaisa.core.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["atEpochMillis"]),
        Index(value = ["categoryId"]),
        Index(value = ["accountId"]),
        Index(value = ["partyId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: TransactionType,
    val title: String,
    val amountPaise: Long,
    val atEpochMillis: Long,
    val categoryId: String?,
    val accountId: String?,
    val partyId: String?,
    val payerId: String?,
    val notes: String?,
    val receiptUri: String?,
    val recurringMeta: String?
) {
    fun toModel() = Transaction(
        id,
        type,
        title,
        amountPaise,
        atEpochMillis,
        categoryId,
        accountId,
        partyId,
        payerId,
        notes,
        receiptUri,
        recurringMeta
    )

    companion object {
        fun from(model: Transaction) = TransactionEntity(
            model.id,
            model.type,
            model.title,
            model.amountPaise,
            model.atEpochMillis,
            model.categoryId,
            model.accountId,
            model.partyId,
            model.payerId,
            model.notes,
            model.receiptUri,
            model.recurringMeta
        )
    }
}
