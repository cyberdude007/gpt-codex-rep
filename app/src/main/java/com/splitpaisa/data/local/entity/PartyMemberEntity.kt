package com.splitpaisa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.splitpaisa.core.model.PartyMember

@Entity(
    tableName = "party_members",
    indices = [Index(value = ["partyId", "displayName"], unique = true)]
)
data class PartyMemberEntity(
    @PrimaryKey val id: String,
    val partyId: String,
    val displayName: String,
    val contact: String?
) {
    fun toModel() = PartyMember(id, partyId, displayName, contact)

    companion object {
        fun from(model: PartyMember) = PartyMemberEntity(model.id, model.partyId, model.displayName, model.contact)
    }
}
