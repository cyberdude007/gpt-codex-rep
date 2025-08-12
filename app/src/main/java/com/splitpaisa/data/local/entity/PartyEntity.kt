package com.splitpaisa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.splitpaisa.core.model.Party

@Entity(
    tableName = "parties",
    indices = [Index(value = ["name"])]
)
data class PartyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long
) {
    fun toModel() = Party(id, name, createdAt)

    companion object {
        fun from(model: Party) = PartyEntity(model.id, model.name, model.createdAt)
    }
}
