package com.splitpaisa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.splitpaisa.core.model.PartyMember
import com.splitpaisa.core.search.TextNormalizer

@Entity(
    tableName = "party_members",
    indices = [
        Index(value = ["partyId", "displayName"], unique = true),
        Index(value = ["normalizedName"])
    ]
)
data class PartyMemberEntity(
    @PrimaryKey val id: String,
    val partyId: String,
    val displayName: String,
    val contact: String?,
    val normalizedName: String,
) {
    fun toModel() = PartyMember(id, partyId, displayName, contact)

    companion object {
        fun from(model: PartyMember) = PartyMemberEntity(
            model.id,
            model.partyId,
            model.displayName,
            model.contact,
            TextNormalizer.normalize(model.displayName)
        )
    }
}
