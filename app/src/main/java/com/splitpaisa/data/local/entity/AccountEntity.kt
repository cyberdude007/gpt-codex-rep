package com.splitpaisa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.splitpaisa.core.model.Account

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String
) {
    fun toModel() = Account(id, name, type)

    companion object {
        fun from(model: Account) = AccountEntity(model.id, model.name, model.type)
    }
}
