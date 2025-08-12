package com.splitpaisa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.splitpaisa.core.model.Split

@Entity(
    tableName = "splits",
    indices = [
        Index(value = ["transactionId", "memberId"])
    ]
)
data class SplitEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val memberId: String,
    val sharePaise: Long
) {
    fun toModel() = Split(id, transactionId, memberId, sharePaise)

    companion object {
        fun from(model: Split) = SplitEntity(model.id, model.transactionId, model.memberId, model.sharePaise)
    }
}
