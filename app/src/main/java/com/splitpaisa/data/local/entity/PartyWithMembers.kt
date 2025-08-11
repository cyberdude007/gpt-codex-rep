package com.splitpaisa.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PartyWithMembers(
    @Embedded val party: PartyEntity,
    @Relation(parentColumn = "id", entityColumn = "partyId")
    val members: List<PartyMemberEntity>
)
