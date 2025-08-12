package com.splitpaisa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.splitpaisa.core.model.Settlement

@Entity(
    tableName = "settlements",
    indices = [
        Index(value = ["partyId"]),
        Index(value = ["payerId"]),
        Index(value = ["payeeId"]),
        Index(value = ["atEpochMillis"])
    ]
)
data class SettlementEntity(
    @PrimaryKey val id: String,
    val partyId: String,
    val payerId: String,
    val payeeId: String,
    val amountPaise: Long,
    val methodNote: String,
    val atEpochMillis: Long,
    val memo: String?
) {
    fun toModel() = Settlement(id, partyId, payerId, payeeId, amountPaise, methodNote, atEpochMillis, memo)

    companion object {
        fun from(model: Settlement) = SettlementEntity(
            model.id,
            model.partyId,
            model.payerId,
            model.payeeId,
            model.amountPaise,
            model.methodNote,
            model.atEpochMillis,
            model.memo
        )
    }
}
